/**
 * Razorpay Checkout helpers.
 *
 * The checkout script is also loaded declaratively in index.html; the loader
 * below is a safety net for environments where that <script> tag is blocked
 * (CSP, ad-blockers, offline dev) — it injects the same CDN script once.
 */

const RAZORPAY_CHECKOUT_URL = 'https://checkout.razorpay.com/v1/checkout.js';

declare global {
  interface Window {
    Razorpay?: new (options: Record<string, unknown>) => { open: () => void };
  }
}

let scriptPromise: Promise<void> | null = null;

/** Ensures the Razorpay Checkout script is present, then resolves. */
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

/** Payment fields Razorpay passes to the modal's success handler. */
export interface RazorpayPaymentResult {
  razorpayOrderId: string;
  razorpayPaymentId: string;
  razorpaySignature: string;
}

export interface RazorpayCheckoutOptions {
  /** Public Razorpay Key ID (from the create-order response). */
  key: string;
  /** Order amount in paise. */
  amount: number;
  currency: string;
  /** Order ID from the create-order endpoint. */
  orderId: string;
  name: string;
  description: string;
  email?: string;
  prefill?: { name?: string; contact?: string };
  themeColor?: string;
  onSuccess: (payment: RazorpayPaymentResult) => void;
  onDismiss?: () => void;
}

/**
 * Loads the checkout script (if needed) and opens the Razorpay payment modal.
 * The success handler receives the raw Razorpay callback fields, which the
 * caller is expected to forward to the backend verify endpoint.
 */
export async function openRazorpayCheckout(options: RazorpayCheckoutOptions): Promise<void> {
  await loadRazorpayScript();

  const Razorpay = window.Razorpay;
  if (!Razorpay) {
    throw new Error('Razorpay checkout is unavailable');
  }

  const rzp = new Razorpay({
    key: options.key,
    amount: options.amount,
    currency: options.currency,
    order_id: options.orderId,
    name: options.name,
    description: options.description,
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
