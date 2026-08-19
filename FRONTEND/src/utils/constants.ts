import type { ApiErrorResponse } from '../types/api';

/**
 * Extracts a human-readable error message from any error shape.
 * Handles Axios errors, standard errors, and API error responses.
 */
export function getErrorMessage(error: unknown): string {
  if (typeof error === 'string') return error;

  // Axios error with API response
  if (error && typeof error === 'object' && 'response' in error) {
    const axiosError = error as { response: { data: ApiErrorResponse } };
    if (axiosError.response?.data?.error?.message) {
      return axiosError.response.data.error.message;
    }
  }

  // Standard Error instance
  if (error instanceof Error) return error.message;

  return 'An unexpected error occurred. Please try again.';
}

/**
 * Extracts field-level validation errors from an API error response.
 */
export function getFieldErrors(error: unknown): Record<string, string> {
  if (error && typeof error === 'object' && 'response' in error) {
    const axiosError = error as { response: { data: ApiErrorResponse } };
    if (axiosError.response?.data?.error?.details) {
      return axiosError.response.data.error.details as Record<string, string>;
    }
  }
  return {};
}

export const STORAGE_KEYS = {
  AUTH_TOKEN: 'cardpro_auth_token',
  REFRESH_TOKEN: 'cardpro_refresh_token',
  USER: 'cardpro_user',
} as const;

export const ROUTES = {
  LOGIN: '/login',
  SIGNUP: '/signup',
  DASHBOARD: '/dashboard',
  CARDS: '/dashboard/cards',
  LEADS: '/dashboard/leads',
  ANALYTICS: '/dashboard/analytics',
  STORE: '/dashboard/store',
  SETTINGS: '/dashboard/settings',
  ADMIN: '/admin',
  /** Public, no-auth digital card viewer. */
  PUBLIC_CARD: (slug: string) => `/c/${slug}`,
} as const;

export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    REFRESH: '/auth/refresh',
    LOGOUT: '/auth/logout',
  },
  CARDS: {
    BASE: '/cards',
    ME: '/cards/me',
    BY_SLUG: (slug: string) => `/cards/${slug}`,
  },
  LEADS: {
    BASE: '/leads',
    FOLLOWUP: (id: string) => `/leads/${id}/followup`,
  },
  AI: {
    GENERATE_BIO: '/api/v1/ai/generate-bio',
    GENERATE_CARD_DETAILS: '/api/v1/ai/generate-card-details',
    UPSCALE_PHOTO: '/api/v1/ai/upscale-photo',
  },
  PAYMENTS: {
    CREATE_ORDER: '/payments/create-order',
    VERIFY: '/payments/verify',
    HISTORY: '/payments/history',
  },
  ANALYTICS: {
    PROFILE: (id: string) => `/analytics/profile/${id}`,
  },
} as const;

export const TOAST_DURATION = 4000;

/**
 * API Gateway base URL resolved dynamically from environment variables
 * with production Render gateway fallback.
 */
export const GATEWAY_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'https://cardpro-ai.onrender.com';

/**
 * Base URL for user-service and direct payment requests.
 * Uses VITE_USER_SERVICE_URL if specified, otherwise routes via Gateway.
 */
export const USER_SERVICE_BASE_URL =
  import.meta.env.VITE_USER_SERVICE_URL || `${GATEWAY_BASE_URL}/api`;

/** localStorage key for the Dark/Light theme preference. */
export const THEME_STORAGE_KEY = 'cardpro_theme';