import api from './api';
import { GATEWAY_BASE_URL } from '../utils/constants';

/**
 * Resolves an avatar/file URL for rendering. blob:, data:, and absolute
 * http(s) URLs pass through unchanged; server-relative /api/... paths are
 * made absolute against the API Gateway so the <img> tag never tries to
 * fetch them from the Vite dev server / static host port (which 404s into
 * a broken-image icon whenever the /api proxy is not in play).
 */
export const resolveAvatarUrl = (url?: string): string => {
  if (!url) return '';
  if (/^(blob:|data:|https?:)/i.test(url)) return url;
  if (url.startsWith('/api/')) return `${GATEWAY_BASE_URL}${url}`;
  return url;
};

export const uploadFile = async (file: File): Promise<string> => {
  try {
    const formData = new FormData();
    formData.append('file', file);

    const response = await api.post('/api/v1/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    return response.data.url;
  } catch (error) {
    // Fallback so card creation still works when the backend file service is
    // unavailable. A base64 data URL is used (NOT URL.createObjectURL) because
    // blob: URLs only live in the browser session that created them — they break
    // on page reload, in the "My Cards" grid, and for public card visitors.
    console.warn('Backend file upload failed. Persisting avatar as a base64 data URL instead.', error);
    return fileToDataUrl(file);
  }
};

/**
 * Reads a File into a base64 data URL so the image persists inside the card's
 * profileData JSON (survives reloads, other devices, and the public viewer).
 */
export const fileToDataUrl = (file: File): Promise<string> =>
  new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = () => reject(new Error('Could not read the image file'));
    reader.readAsDataURL(file);
  });