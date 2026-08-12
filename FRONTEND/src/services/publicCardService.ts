import api from './api';

/**
 * Response shape of GET /api/v1/cards/{slug} (card-service PublicCardResponse).
 * `profileData` is a JSON string — parse it with `parseProfileData`.
 */
export interface PublicCardResponse {
  /** Profile (card) UUID — used to attribute lead submissions to the card owner. */
  id?: string | null;
  slug: string;
  templateId: string | null;
  profileData: string | null;
  aiAvatarUrl?: string | null;
}

/** Fields stored inside the profileData JSON (mirrors the Create Card form + SRS). */
export interface PublicCardProfile {
  fullName?: string;
  title?: string;
  bio?: string;
  avatarUrl?: string;
  phone?: string;
  email?: string;
  skills?: string[];
  linkedin?: string;
  github?: string;
  website?: string;
  portfolio?: string;
  youtube?: string;
  instagram?: string;
  twitter?: string;
  address?: string;
  appointmentUrl?: string;
  [key: string]: unknown;
}

/**
 * Fetches a public card profile by slug.
 *
 * This call is safe for unauthenticated visitors: it passes `skipAuthRedirect`
 * so a 401 from the API is returned to the caller instead of the global Axios
 * interceptor bouncing the user to /login.
 */
export async function fetchPublicCard(slug: string): Promise<PublicCardResponse> {
  const response = await api.get(`/api/v1/cards/${encodeURIComponent(slug)}`, {
    skipAuthRedirect: true,
  });
  return response.data;
}

/** Parses the profileData JSON string into a typed object (never throws). */
export function parseProfileData(profileData: string | null): PublicCardProfile {
  if (!profileData) return {};
  try {
    const parsed = JSON.parse(profileData);
    return typeof parsed === 'object' && parsed !== null ? (parsed as PublicCardProfile) : {};
  } catch {
    return {};
  }
}

/** Payload submitted by a visitor to the card owner (POST /api/v1/leads). */
export interface SubmitLeadPayload {
  profileId: string;
  visitorName: string;
  visitorEmail: string;
  visitorPhone?: string;
  message?: string;
}

/**
 * Submits a visitor's contact info so it is attributed to the card owner.
 *
 * Like `fetchPublicCard`, this is safe for unauthenticated visitors — a 401 is
 * returned to the caller instead of the global interceptor redirecting to /login.
 */
export async function submitLead(payload: SubmitLeadPayload): Promise<void> {
  await api.post('/api/v1/leads', payload, {
    skipAuthRedirect: true,
  });
}
