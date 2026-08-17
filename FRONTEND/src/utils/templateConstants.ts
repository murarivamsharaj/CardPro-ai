import { DEFAULT_CARD_GRADIENT } from './colorUtils';

/**
 * Shared card design templates — used by the Create Card editor, the Edit
 * Card modal, and the public card viewer. Premium templates gate behind the
 * Store's "Premium Templates" purchase (the premiumTemplatesUnlocked flag on
 * the user's card).
 */
export interface Template {
  id: string;
  name: string;
  description: string;
  /** CSS background shorthand used for the swatch and the card gradient. */
  gradient: string;
  premium: boolean;
}

export const TEMPLATES: Template[] = [
  { id: 'default', name: 'Classic', description: 'Clean & timeless', gradient: 'linear-gradient(135deg, #312e81, #7c3aed)', premium: false },
  { id: 'minimal', name: 'Minimal', description: 'Subtle & elegant', gradient: 'linear-gradient(135deg, #1e293b, #475569)', premium: false },
  { id: 'bold', name: 'Bold', description: 'Vivid & energetic', gradient: 'linear-gradient(135deg, #be123c, #f97316)', premium: false },
  { id: 'aurora', name: 'Aurora', description: 'Premium gradient flow', gradient: 'linear-gradient(135deg, #6d28d9, #d946ef)', premium: true },
  { id: 'neon', name: 'Neon', description: 'Premium dark neon', gradient: 'linear-gradient(135deg, #0f172a, #22d3ee)', premium: true },
  { id: 'gold', name: 'Gold', description: 'Premium luxury finish', gradient: 'linear-gradient(135deg, #713f12, #fbbf24)', premium: true },
];

export const DEFAULT_TEMPLATE_ID = 'default';

export const TEMPLATE_BY_ID: Record<string, Template> = Object.fromEntries(
  TEMPLATES.map((t) => [t.id, t])
);

/** Gradient for the selected template, falling back to the default card gradient. */
export function templateGradient(templateId: string): string {
  return TEMPLATE_BY_ID[templateId]?.gradient || DEFAULT_CARD_GRADIENT;
}
