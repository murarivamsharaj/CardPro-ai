const RAZORPAY_CHECKOUT_URL = 'https://checkout.razorpay.com/v1/checkout.js';

declare global {
  interface Window {
    Razorpay?: new (options: Record<string, unknown>) => { open: () => void };
  }
}

let scriptPromise: Promise<void> | null = null;

export function loadRazorpayScript(): Promise<void> {
  if (window.Razorpay) return Promise.resolve();
  if (scriptPromise) return scriptPromise;

  scriptPromise = new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.src = RAZORPAY_CHECKOUT_URL;
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () => {
      scriptPromise = null;
      reject(new Error('Razorpay checkout script failed to load. Check your internet connection.'));
    };
    document.head.appendChild(script);
  });

  return scriptPromise;
}

export interface RazorpayPaymentResult {
  razorpayOrderId: string;
  razorpayPaymentId: string;
  razorpaySignature: string;
}

export interface RazorpayCheckoutOptions {
  key?: string;
  amount: number;
  currency: string;
  orderId: string;
  name?: string;
  description?: string;
  email?: string;
  prefill?: { name?: string; contact?: string };
  themeColor?: string;
  onSuccess: (payment: RazorpayPaymentResult) => void;
  onDismiss?: () => void;
}

export async function openRazorpayCheckout(options: RazorpayCheckoutOptions): Promise<void> {
  await loadRazorpayScript();

  const Razorpay = window.Razorpay;
  if (!Razorpay) {
    throw new Error('Razorpay checkout is unavailable');
  }

  // Guaranteed fallback so the modal never initializes without an auth key
  const finalKey = options.key || (import.meta.env.VITE_RAZORPAY_KEY_ID as string) || 'rzp_test_TQnTopgwwCYbNN';

  const rzp = new Razorpay({
    key: finalKey,
    amount: options.amount,
    currency: options.currency || 'INR',
    order_id: options.orderId,
    name: options.name || 'CardPro AI',
    description: options.description || 'CardPro Pro Plan',
    prefill: {
      email: options.email,
      ...options.prefill,
    },
    theme: { color: options.themeColor || '#7c3aed' },
    handler: (response: { razorpay_order_id?: string; razorpay_payment_id?: string; razorpay_signature?: string }) => {
      options.onSuccess({
        razorpayOrderId: response.razorpay_order_id || '',
        razorpayPaymentId: response.razorpay_payment_id || '',
        razorpaySignature: response.razorpay_signature || '',
      });
    },
    modal: {
      ondismiss: () => options.onDismiss?.(),
    },
  });

  rzp.open();
}