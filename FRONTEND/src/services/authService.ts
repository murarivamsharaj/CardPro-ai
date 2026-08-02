import api from './api';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: {
    id: string;
    email: string;
    leadCredits: number;
  };
}

export const authService = {
  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await api.post('/api/v1/auth/login', data);
    // Check for both token or accessToken in case the backend property name varies
    const token = response.data.token || (response.data as any).accessToken;
    if (token) {
      localStorage.setItem('token', token);
    }
    return response.data;
  },

  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post('/api/v1/auth/register', data);
    const token = response.data.token || (response.data as any).accessToken;
    if (token) {
      localStorage.setItem('token', token);
    }
    return response.data;
  },

  logout() {
    localStorage.removeItem('token');
  },
};