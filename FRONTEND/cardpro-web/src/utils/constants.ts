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
    GENERATE_BIO: '/ai/generate-bio',
    UPSCALE_PHOTO: '/ai/upscale-photo',
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
