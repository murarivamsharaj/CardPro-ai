import api from './api';

/**
 * Response shape of GET /api/v1/cards/{slug} (card-service PublicCardResponse).
 * `profileData` is a JSON string — parse it with `parseProfileData`.
 */
export interface PublicCardResponse {
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
