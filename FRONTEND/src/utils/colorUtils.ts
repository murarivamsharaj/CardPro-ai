/**
 * Smart Color Coordination
 * Extracts the primary color from a user's uploaded logo/avatar and
 * produces a coordinated gradient for their digital card — the visual
 * equivalent of matching a pocket square to a formal jacket.
 */

export interface RGB {
  r: number;
  g: number;
  b: number;
}

/** Fallback gradient used when no image / extraction fails. */
export const DEFAULT_CARD_GRADIENT =
  'linear-gradient(135deg, #7c3aed 0%, #db2777 100%)';

function rgbToHex({ r, g, b }: RGB): string {
  const toHex = (v: number) => Math.max(0, Math.min(255, Math.round(v))).toString(16).padStart(2, '0');
  return `#${toHex(r)}${toHex(g)}${toHex(b)}`;
}

export function hexToRgb(hex: string): RGB | null {
  const m = hex.replace('#', '');
  if (!/^[0-9a-fA-F]{6}$/.test(m)) return null;
  return {
    r: parseInt(m.slice(0, 2), 16),
    g: parseInt(m.slice(2, 4), 16),
    b: parseInt(m.slice(4, 6), 16),
  };
}

/** Whether a color is light — used to pick readable text color on cards. */
export function isLightColor(hex: string): boolean {
  const rgb = hexToRgb(hex);
  if (!rgb) return true;
  // Perceived luminance (WCAG-ish)
  const lum = 0.299 * rgb.r + 0.587 * rgb.g + 0.114 * rgb.b;
  return lum > 160;
}

/**
 * Build a premium 2-stop gradient from a base color.
 * The complementary end is derived by rotating hue by ~40° for depth.
 */
export function buildCardGradient(hex: string): string {
  const rgb = hexToRgb(hex);
  if (!rgb) return DEFAULT_CARD_GRADIENT;

  const [h, s, l] = rgbToHsl(rgb);
  const endHue = (h + 40) % 360;

  const start = hslToHex(h, Math.min(1, s), Math.max(0.12, Math.min(0.35, l)));
  const end = hslToHex(endHue, Math.min(1, s), Math.max(0.4, Math.min(0.6, l)));

  return `linear-gradient(135deg, ${start} 0%, ${end} 100%)`;
}

/**
 * Extract the dominant color from an image URL by sampling downscaled
 * pixels on a canvas. Returns a hex string; falls back on any failure
 * (CORS-tainted canvas, invalid image, etc.).
 */
export async function extractPrimaryColor(imageUrl: string): Promise<string> {
  try {
    const img = await loadImage(imageUrl);
    const canvas = document.createElement('canvas');
    // Downscale large images for performance and to emphasize large areas.
    const size = 48;
    canvas.width = size;
    canvas.height = size;
    const ctx = canvas.getContext('2d', { willReadFrequently: true });
    if (!ctx) return DEFAULT_CARD_GRADIENT;
    ctx.drawImage(img, 0, 0, size, size);

    const { data } = ctx.getImageData(0, 0, size, size);

    // Bucket pixels by quantized color, then average the winning bucket.
    const buckets = new Map<number, { count: number; r: number; g: number; b: number }>();
    for (let i = 0; i < data.length; i += 4) {
      const a = data[i + 3];
      if (a < 128) continue; // skip transparent pixels
      const r = data[i];
      const g = data[i + 1];
      const b = data[i + 2];
      // Skip near-white / near-black noise so the brand color wins.
      const lum = 0.299 * r + 0.587 * g + 0.114 * b;
      if (lum < 20 || lum > 245) continue;

      const key = ((r >> 5) << 10) | ((g >> 5) << 5) | (b >> 5);
      const bucket = buckets.get(key);
      if (bucket) {
        bucket.count += 1;
        bucket.r += r;
        bucket.g += g;
        bucket.b += b;
      } else {
        buckets.set(key, { count: 1, r, g, b });
      }
    }

    if (buckets.size === 0) return DEFAULT_CARD_GRADIENT;

    let best: { count: number; r: number; g: number; b: number } | null = null;
    for (const bucket of buckets.values()) {
      if (!best || bucket.count > best.count) best = bucket;
    }
    if (!best) return DEFAULT_CARD_GRADIENT;

    return rgbToHex({
      r: best.r / best.count,
      g: best.g / best.count,
      b: best.b / best.count,
    });
  } catch {
    return DEFAULT_CARD_GRADIENT;
  }
}

function loadImage(url: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => resolve(img);
    img.onerror = () => reject(new Error('Failed to load image'));
    img.src = url;
  });
}

function rgbToHsl({ r, g, b }: RGB): [number, number, number] {
  const rn = r / 255;
  const gn = g / 255;
  const bn = b / 255;
  const max = Math.max(rn, gn, bn);
  const min = Math.min(rn, gn, bn);
  const l = (max + min) / 2;
  if (max === min) return [0, 0, l];

  const d = max - min;
  const s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
  let h = 0;
  if (max === rn) h = ((gn - bn) / d + (gn < bn ? 6 : 0)) / 6;
  else if (max === gn) h = ((bn - rn) / d + 2) / 6;
  else h = ((rn - gn) / d + 4) / 6;
  return [h * 360, s, l];
}

function hslToHex(h: number, s: number, l: number): string {
  const c = (1 - Math.abs(2 * l - 1)) * s;
  const x = c * (1 - Math.abs(((h / 60) % 2) - 1));
  const m = l - c / 2;
  let r = 0;
  let g = 0;
  let b = 0;
  if (h < 60) [r, g, b] = [c, x, 0];
  else if (h < 120) [r, g, b] = [x, c, 0];
  else if (h < 180) [r, g, b] = [0, c, x];
  else if (h < 240) [r, g, b] = [0, x, c];
  else if (h < 300) [r, g, b] = [x, 0, c];
  else [r, g, b] = [c, 0, x];
  return rgbToHex({
    r: (r + m) * 255,
    g: (g + m) * 255,
    b: (b + m) * 255,
  });
}
