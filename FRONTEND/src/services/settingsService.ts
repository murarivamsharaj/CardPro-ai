import axios from 'axios';
import api from './api';
import { analyticsService } from './analyticsService';
import { USER_SERVICE_BASE_URL, STORAGE_KEYS } from '../utils/constants';

/**
 * Dedicated axios instance for direct/gateway calls to user-service (profile details
 * + notification preferences).
 */
const userApi = axios.create({
  baseURL: USER_SERVICE_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

userApi.interceptors.request.use((config) => {
  // Support both custom storage key and standard fallback
  const token =
    localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN) ||
    localStorage.getItem('token') ||
    localStorage.getItem('cardpro_auth_token');

  if (token && token !== 'undefined' && token !== 'null' && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/* ─────────────────────────── Types ─────────────────────────── */

export interface UserProfile {
  id?: number;
  email: string;
  firstName?: string;
  lastName?: string;
  displayName?: string;
  jobTitle?: string;
  phoneNumber?: string;
  role?: string;
  active?: boolean;
  /** Whether the user holds a CardPro Pro subscription. */
  pro?: boolean;
  /** Pro perk: hides the "Powered by CardPro" watermark on public cards. */
  removeWatermark?: boolean;
  /** Developer integration: auto-generated API key (regenerable). */
  apiKey?: string;
  /** Developer integration: webhook URL for future CRM lead-forwarding. */
  webhookUrl?: string;
  emailNotificationsEnabled?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminUser {
  id: string;
  email: string;
  role: 'USER' | 'ADMIN';
  leadCredits?: number;
  enabled?: boolean;
  createdAt?: string;
}

export interface RegistrationConfig {
  enabled: boolean;
}

export interface TotalCardsResponse {
  totalCards: number;
}

/* ─────────────────── user-service (direct) ─────────────────── */

export const getMyProfile = async (): Promise<UserProfile> => {
  const { data } = await userApi.get<UserProfile>('/users/me');
  return data;
};

export const updateMyProfile = async (profile: {
  displayName?: string;
  phoneNumber?: string;
  jobTitle?: string;
  removeWatermark?: boolean;
  webhookUrl?: string;
}): Promise<UserProfile> => {
  const { data } = await userApi.put<UserProfile>('/users/profile', profile);
  return data;
};

export const updateEmailNotifications = async (enabled: boolean): Promise<UserProfile> => {
  const { data } = await userApi.put<UserProfile>('/users/notifications', { enabled });
  return data;
};

/** Generate a fresh developer API key (the previous one stops working). */
export const regenerateApiKey = async (): Promise<UserProfile> => {
  const { data } = await userApi.post<UserProfile>('/users/api-key/regenerate');
  return data;
};

/** Save (or clear) the CRM lead-forwarding webhook URL. */
export const updateWebhookUrl = async (webhookUrl: string): Promise<UserProfile> => {
  const { data } = await userApi.put<UserProfile>('/users/webhook', { webhookUrl });
  return data;
};

/** Soft-delete the caller's own account. */
export const deleteMyAccount = async (): Promise<UserProfile> => {
  const { data } = await userApi.delete<UserProfile>('/users/me');
  return data;
};

/* ─────────────── payments (user-service / gateway) ─────────────── */

export interface CreateOrderResponse {
  orderId: string;
  /** Public Razorpay Key ID used to open the Checkout modal. */
  keyId: string;
  /** Order amount in paise (₹999 → 99900). */
  amount: number;
  currency: string;
  status: string;
}

export interface VerifyPaymentPayload {
  razorpayOrderId: string;
  razorpayPaymentId: string;
  /** Razorpay signature returned by the Checkout handler. */
  signature: string;
}

export interface VerifyPaymentResponse {
  success: boolean;
  message: string;
  pro: boolean;
}

/** Create a Razorpay order for the ₹999 CardPro Pro upgrade. */
export const createProOrder = async (): Promise<CreateOrderResponse> => {
  const { data } = await userApi.post<CreateOrderResponse>('/users/payments/create-order');
  return data;
};

/** Verify the Razorpay signature server-side; on success activates Pro. */
export const verifyProPayment = async (payload: VerifyPaymentPayload): Promise<VerifyPaymentResponse> => {
  const { data } = await userApi.post<VerifyPaymentResponse>('/users/payments/verify', payload);
  return data;
};

/* ─────────────── auth-service (via gateway) ────────────────── */

export const changePassword = async (currentPassword: string, newPassword: string): Promise<void> => {
  await api.post('/api/v1/auth/change-password', { currentPassword, newPassword });
};

export const getAllUsers = async (): Promise<AdminUser[]> => {
  const { data } = await api.get<AdminUser[]>('/api/v1/auth/admin/users');
  return data;
};

export const updateUserRole = async (userId: string, role: 'USER' | 'ADMIN'): Promise<AdminUser> => {
  const { data } = await api.patch<AdminUser>(`/api/v1/auth/admin/users/${userId}/role`, { role });
  return data;
};

export const updateUserStatus = async (userId: string, enabled: boolean): Promise<AdminUser> => {
  const { data } = await api.patch<AdminUser>(`/api/v1/auth/admin/users/${userId}/status`, { enabled });
  return data;
};

export const softDeleteUser = async (userId: string): Promise<void> => {
  await api.delete(`/api/v1/auth/admin/users/${userId}`);
};

export const getRegistrationConfig = async (): Promise<RegistrationConfig> => {
  const { data } = await api.get<RegistrationConfig>('/api/v1/auth/admin/config/registration');
  return data;
};

export const setRegistrationConfig = async (enabled: boolean): Promise<RegistrationConfig> => {
  const { data } = await api.put<RegistrationConfig>('/api/v1/auth/admin/config/registration', { enabled });
  return data;
};

/* ─────────────── card-service (via gateway) ────────────────── */

export const getTotalCardCount = async (): Promise<TotalCardsResponse> => {
  return analyticsService.getTotalCards();
};