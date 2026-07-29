import api from './api';
import type { AuthResponse, LoginRequest, RegisterRequest, RefreshTokenRequest } from '@/types/auth';
import { API_ENDPOINTS } from '@/utils/constants';

const authService = {
  /**
   * Register a new user account.
   */
  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>(API_ENDPOINTS.AUTH.REGISTER, data);
    return response.data;
  },

  /**
   * Authenticate with email and password.
   */
  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>(API_ENDPOINTS.AUTH.LOGIN, data);
    return response.data;
  },

  /**
   * Refresh the access token using a valid refresh token.
   */
  async refreshToken(data: RefreshTokenRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>(API_ENDPOINTS.AUTH.REFRESH, data);
    return response.data;
  },

  /**
   * Logout the current user (invalidates tokens on the server).
   */
  async logout(): Promise<void> {
    await api.post(API_ENDPOINTS.AUTH.LOGOUT);
  },
};

export default authService;
