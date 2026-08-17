import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchMyCards } from '../../store/slices/cardSlice';
import {
  ArrowRight,
  Check,
  Globe,
  ImageUp,
  LayoutTemplate,
  Nfc,
  ShoppingCart,
  Users,
  X,
  type LucideIcon,
} from 'lucide-react';
import { notifyError, notifyInfo } from '../../store/useNotificationStore';
import api from '../../services/api';

/**
 * CardPro AI — Store
 * Freemium microtransactions: premium templates, the NFC hardware card,
 * custom domains, lead packs, and AI photo upscaling. Each product opens a
 * center-screen glass modal with a glowing visual, feature list, and price.
 */

type ProductType = 'One-Time' | 'Consumable';

interface Product {
  id: string;
  name: string;
  price: number; // in ₹
  type: ProductType;
  description: string;
  features: string[];
  icon: LucideIcon;
  gradient: string; // Tailwind gradient stops (kept as literals for JIT)
}

/** Official freemium catalogue (prices in ₹). */
const PRODUCTS: Product[] = [
  {
    id: 'premium-templates',
    name: 'Premium Templates',
    price: 149,
    type: 'One-Time',
    description: 'Unlock professional card designs.',
    features: [
      'Exclusive collection of premium digital card layouts',
      'Designed for executives, creators, and founders',
      'Works with your existing profile instantly',
      'Free lifetime updates as new templates ship',
    ],
    icon: LayoutTemplate,
    gradient: 'from-violet-600 to-purple-600',
  },
  {
    id: 'nfc-smart-card',
    name: 'NFC Smart Card (Hardware)',
    price: 999,
    type: 'One-Time',
    description: 'Physical tap-to-share card linked to your profile.',
    features: [
      'Premium physical card with embedded NFC chip',
      'Tap to any phone and share your card instantly',
      'Permanently linked to your CardPro AI profile',
      'Sleek, durable, pocket-sized — ships to you',
    ],
    icon: Nfc,
    gradient: 'from-fuchsia-600 to-pink-600',
  },
  {
    id: 'custom-domain',
    name: 'Custom Domain',
    price: 499,
    type: 'One-Time',
    description: 'Use your own custom web address.',
    features: [
      'Point your own domain to your digital card',
      'Personalized link your clients will remember',
      'Automatic HTTPS with no configuration',
      'Keeps your branding consistent everywhere',
    ],
    icon: Globe,
    gradient: 'from-indigo-600 to-violet-600',
  },
  {
    id: 'lead-pack',
    name: 'Lead Pack (100 Credits)',
    price: 199,
    type: 'Consumable',
    description: 'Boost your lead capture limits.',
    features: [
      '100 lead capture credits added to your account',
      'See who viewed your card and when',
      'Convert visitors into conversations',
      'Credits never expire — use them at your pace',
    ],
    icon: Users,
    gradient: 'from-emerald-600 to-teal-600',
  },
  {
    id: 'ai-photo-upscale',
    name: 'AI Photo Upscale',
    price: 49,
    type: 'One-Time',
    description: '4K resolution enhancement for avatars.',
    features: [
      'AI-powered enhancement up to stunning 4K clarity',
      'Removes noise and sharpens facial details',
      'Perfect for profile photos and logos',
      'Applied instantly to your avatar',
    ],
    icon: ImageUp,
    gradient: 'from-sky-600 to-indigo-600',
  },
];

const TYPE_BADGE: Record<ProductType, string> = {
  'One-Time': 'border-violet-400/30 bg-violet-500/20 text-violet-200',
  Consumable: 'border-emerald-400/30 bg-emerald-500/20 text-emerald-200',
};

const CARD_CLASSES =
  'group flex h-full w-full flex-col bg-white/5 border border-white/10 backdrop-blur-md rounded-2xl p-6 text-left ' +
  'transition-all duration-300 hover:-translate-y-2 hover:shadow-[0_0_30px_rgba(192,38,211,0.3)] hover:border-fuchsia-500/50';

/** Maps Store product ids to the payment-service ItemType enum. */
const ITEM_TYPE_BY_PRODUCT: Record<string, string> = {
  'premium-templates': 'TEMPLATE',
  'nfc-smart-card': 'NFC',
  'custom-domain': 'CUSTOM_DOMAIN',
  'lead-pack': 'LEAD_PACK',
  'ai-photo-upscale': 'AI_PHOTO',
};

/** Shape of POST /api/v1/payments/create-order. */
interface CreateOrderResponse {
  orderId: string;
  razorpayKeyId: string;
  amount: number; // in paise
  currency: string;
  status: string;
}

/** Minimal Razorpay Checkout options (https://razorpay.com/docs/payments/checkout/). */
interface RazorpayOptions {
  key: string;
  amount: number;
  currency: string;
  name: string;
  description: string;
  order_id: string;
  handler: (response: {
    razorpay_payment_id: string;
    razorpay_order_id: string;
    razorpay_signature: string;
  }) => void;
  theme?: { color?: string };
}

/** Cached promise so checkout.js is fetched at most once per page session. */
let razorpayScriptPromise: Promise<void> | null = null;

function loadRazorpayScript(): Promise<void> {
  if ((window as any).Razorpay) return Promise.resolve();
  if (!razorpayScriptPromise) {
    razorpayScriptPromise = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.async = true;
      script.onload = () => resolve();
      script.onerror = () => {
        razorpayScriptPromise = null; // allow a retry on the next click
        reject(new Error('Failed to load the Razorpay checkout script'));
      };
      document.body.appendChild(script);
    });
  }
  return razorpayScriptPromise;
}

export const StorePage: React.FC = () => {
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [purchasing, setPurchasing] = useState(false);
  const dispatch = useDispatch<any>();
  // The user's own card carries the purchased-entitlement flags (one card per
  // user is normalized into an array by fetchMyCards).
  const currentCard = useSelector((state: any) => state.card.cards?.[0] || null);

  useEffect(() => {
    dispatch(fetchMyCards());
  }, [dispatch]);

  /** True when the product is already owned (currently: Premium Templates). */
  const isUnlocked = (product: Product) =>
    product.id === 'premium-templates' && !!currentCard?.premiumTemplatesUnlocked;

  // Close on Escape + lock page scroll while the modal is open.
  useEffect(() => {
    if (!selectedProduct) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setSelectedProduct(null);
    };
    window.addEventListener('keydown', onKey);
    document.body.style.overflow = 'hidden';
    return () => {
      window.removeEventListener('keydown', onKey);
      document.body.style.overflow = '';
    };
  }, [selectedProduct]);

  const handlePurchase = async (product: Product) => {
    if (purchasing) return;
    if (isUnlocked(product)) {
      notifyInfo('Already unlocked', 'Premium Templates are already available on your account.');
      return;
    }
    setPurchasing(true);
    try {
      // A. Ensure the Razorpay checkout script is loaded.
      await loadRazorpayScript();

      // B. Ask the backend to create a Razorpay order for this product.
      const { data } = await api.post<CreateOrderResponse>('/api/v1/payments/create-order', {
        itemType: ITEM_TYPE_BY_PRODUCT[product.id],
        amount: product.price,
      });

      // C. Extract order details from the backend response.
      const { orderId, razorpayKeyId, amount, currency } = data;

      // D. Initialize the Razorpay checkout with the real order.
      const options: RazorpayOptions = {
        key: razorpayKeyId,
        amount,
        currency,
        name: 'CardPro AI',
        description: product.name,
        order_id: orderId,
        theme: { color: '#c026d3' },
        handler: async (response) => {
          // E. Verify the payment signature on the backend before unlocking.
          notifyInfo('Verifying Payment', 'Please wait while we confirm your transaction...');
          try {
            await api.post('/api/v1/payments/verify', {
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              signature: response.razorpay_signature,
            });
            // Re-fetch the card so the newly unlocked entitlement reflects in
            // the grid/modal instantly, without a page reload.
            dispatch(fetchMyCards());
            notifyInfo('Payment Verified!', 'Your purchase is complete and your products are unlocked.');
            setSelectedProduct(null);
          } catch (error) {
            notifyError(
              'Verification Failed',
              'There was an issue verifying your payment. Please contact support.'
            );
          }
        },
      };

      const rzp = new (window as any).Razorpay(options);
      rzp.open();
    } catch (error) {
      notifyError('Checkout failed', 'Could not start the payment. Please try again.');
    } finally {
      setPurchasing(false);
    }
  };

  return (
    <div className="mx-auto max-w-7xl p-6 lg:p-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold tracking-tight text-white">Store</h1>
        <p className="mt-1 text-sm text-white/50">
          Premium upgrades, hardware, and AI tools to take your digital card further.
        </p>
      </div>

      {/* Product grid */}
      <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
        {PRODUCTS.map((product) => {
          const Icon = product.icon;
          return (
            <button
              key={product.id}
              type="button"
              onClick={() => setSelectedProduct(product)}
              className={CARD_CLASSES}
            >
              <div className="flex items-start justify-between gap-3">
                <div
                  className={`flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br ${product.gradient} text-white shadow-lg shadow-black/30 transition-transform duration-300 group-hover:scale-110`}
                >
                  <Icon className="h-6 w-6" strokeWidth={1.6} />
                </div>
                <span
                  className={`rounded-full border px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider backdrop-blur-md ${TYPE_BADGE[product.type]}`}
                >
                  {product.type}
                </span>
              </div>

              <h3 className="mt-4 text-lg font-semibold text-white">{product.name}</h3>
              <p className="mt-1 text-sm text-white/50">{product.description}</p>
              {product.id === 'lead-pack' && (
                <p className="mt-2 inline-flex items-center gap-1.5 rounded-full border border-emerald-400/30 bg-emerald-500/15 px-2.5 py-0.5 text-[11px] font-semibold text-emerald-200 backdrop-blur-md">
                  Current Balance: {currentCard?.leadCredits || 0} Credits
                </p>
              )}

              <div className="mt-5 flex items-end justify-between border-t border-white/10 pt-4">
                {isUnlocked(product) ? (
                  <span className="inline-flex items-center gap-1.5 rounded-full border border-emerald-400/30 bg-emerald-500/20 px-3 py-1 text-xs font-bold text-emerald-200 backdrop-blur-md">
                    Unlocked ✅
                  </span>
                ) : (
                  <span className="text-xl font-bold text-white">
                    <span className="align-top text-sm font-semibold text-fuchsia-300">₹</span>
                    {product.price}
                  </span>
                )}
                <span className="flex items-center gap-1 text-xs font-semibold text-fuchsia-300 transition-all duration-300 group-hover:gap-2">
                  View Details
                  <ArrowRight className="h-3.5 w-3.5" />
                </span>
              </div>
            </button>
          );
        })}
      </div>

      {/* Center-screen product modal */}
      {selectedProduct && (
        <ProductModal
          product={selectedProduct}
          purchasing={purchasing}
          unlocked={isUnlocked(selectedProduct)}
          onClose={() => setSelectedProduct(null)}
          onPurchase={handlePurchase}
        />
      )}
    </div>
  );
};

/** Full-screen blurred overlay + wide side-by-side glass modal. */
function ProductModal({
  product,
  purchasing,
  unlocked,
  onClose,
  onPurchase,
}: {
  product: Product;
  purchasing: boolean;
  unlocked: boolean;
  onClose: () => void;
  onPurchase: (product: Product) => void;
}) {
  const Icon = product.icon;

  return (
    <div className="modal-fade-in fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm">
      <div className="absolute inset-0" onClick={onClose} />

      <div
        role="dialog"
        aria-modal="true"
        aria-label={product.name}
        className="modal-zoom-in relative w-full max-w-4xl overflow-hidden rounded-2xl border border-white/15 bg-slate-900/90 shadow-2xl shadow-black/50 backdrop-blur-2xl"
      >
        {/* Close (X) */}
        <button
          onClick={onClose}
          aria-label="Close"
          className="absolute right-4 top-4 z-10 flex h-9 w-9 items-center justify-center rounded-full border border-white/15 bg-slate-900/60 text-white/60 backdrop-blur-md transition-all duration-200 hover:bg-white/15 hover:text-white active:scale-90"
        >
          <X className="h-5 w-5" />
        </button>

        <div className="flex flex-col md:flex-row">
          {/* Left — large glowing product visual */}
          <div
            className={`relative flex shrink-0 items-center justify-center overflow-hidden bg-gradient-to-br ${product.gradient} p-12 md:w-2/5 md:p-16`}
          >
            {/* Ambient glow */}
            <div className="pointer-events-none absolute inset-0">
              <div className="absolute -left-12 -top-12 h-44 w-44 rounded-full bg-white/25 blur-3xl" />
              <div className="absolute -bottom-16 -right-10 h-52 w-52 rounded-full bg-black/20 blur-3xl" />
            </div>
            <div className="relative flex h-36 w-36 items-center justify-center rounded-3xl border border-white/25 bg-white/10 shadow-2xl shadow-black/40 backdrop-blur-xl">
              <Icon className="h-20 w-20 text-white drop-shadow-lg" strokeWidth={1.3} />
            </div>
            <span className="absolute bottom-5 left-1/2 -translate-x-1/2 rounded-full border border-white/25 bg-black/25 px-3 py-1 text-xs font-bold text-white backdrop-blur-md">
              ₹{product.price}
            </span>
          </div>

          {/* Right — details */}
          <div className="flex-1 p-6 sm:p-8">
            <span
              className={`inline-flex rounded-full border px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider backdrop-blur-md ${TYPE_BADGE[product.type]}`}
            >
              {product.type}
            </span>
            <h2 className="mt-3 text-2xl font-bold tracking-tight text-white">{product.name}</h2>
            <p className="mt-2 text-sm leading-relaxed text-white/60">{product.description}</p>

            <ul className="mt-5 space-y-2.5">
              {product.features.map((feature) => (
                <li key={feature} className="flex items-start gap-2.5 text-sm text-white/75">
                  <span
                    className={`mt-0.5 flex h-4 w-4 shrink-0 items-center justify-center rounded-full bg-gradient-to-br ${product.gradient}`}
                  >
                    <Check className="h-3 w-3 text-white" strokeWidth={3} />
                  </span>
                  {feature}
                </li>
              ))}
            </ul>

            <div className="mt-7 flex flex-wrap items-center justify-between gap-4 border-t border-white/10 pt-5">
              <div>
                <p className="text-xs uppercase tracking-wider text-white/40">Price</p>
                <p className="text-3xl font-bold text-white">
                  <span className="align-top text-lg font-semibold text-fuchsia-300">₹</span>
                  {product.price}
                  <span className="ml-2 text-xs font-medium text-white/40">{product.type}</span>
                </p>
              </div>
              <button
                onClick={() => onPurchase(product)}
                disabled={purchasing || unlocked}
                className="btn-primary"
              >
                <ShoppingCart className="h-4 w-4" />
                {unlocked ? 'Unlocked ✅' : purchasing ? 'Starting Checkout…' : 'Purchase / Add to Cart'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default StorePage;
