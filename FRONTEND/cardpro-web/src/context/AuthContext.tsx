import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import type { User, LoginRequest, RegisterRequest } from '@/types/auth';
import authService from '@/services/authService';
import { STORAGE_KEYS } from '@/utils/constants';
import { getErrorMessage } from '@/utils/api';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (data: LoginRequest) => Promise<void>;
  signup: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // ──────────────────────────────────────────────
  // Session Persistence — Load on Mount
  // ──────────────────────────────────────────────

  useEffect(() => {
    try {
      const storedUser = localStorage.getItem(STORAGE_KEYS.USER);
      const storedToken = localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);

      if (storedUser && storedToken) {
        const parsedUser = JSON.parse(storedUser) as User;
        setUser(parsedUser);
        setToken(storedToken);
      }
    } catch {
      // Corrupted localStorage — clear and start fresh
      clearStorage();
    } finally {
      setIsLoading(false);
    }
  }, []);

  // ──────────────────────────────────────────────
  // Storage Helpers
  // ──────────────────────────────────────────────

  function persistAuth(responseToken: string, responseRefreshToken: string, responseUser: User) {
    localStorage.setItem(STORAGE_KEYS.AUTH_TOKEN, responseToken);
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, responseRefreshToken);
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(responseUser));
    setToken(responseToken);
    setUser(responseUser);
  }

  function clearStorage() {
    localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
    setToken(null);
    setUser(null);
  }

  // ──────────────────────────────────────────────
  // Actions
  // ──────────────────────────────────────────────

  const login = useCallback(async (data: LoginRequest) => {
    setError(null);
    setIsLoading(true);
    try {
      const response = await authService.login(data);
      persistAuth(response.token, response.refreshToken, response.user);
    } catch (err) {
      const message = getErrorMessage(err);
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const signup = useCallback(async (data: RegisterRequest) => {
    setError(null);
    setIsLoading(true);
    try {
      const response = await authService.register(data);
      persistAuth(response.token, response.refreshToken, response.user);
    } catch (err) {
      const message = getErrorMessage(err);
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    setError(null);
    try {
      await authService.logout();
    } catch {
      // Even if the server request fails, clear local state
    } finally {
      clearStorage();
    }
  }, []);

  const clearError = useCallback(() => setError(null), []);

  // ──────────────────────────────────────────────
  // Derived State
  // ──────────────────────────────────────────────

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated: !!user && !!token,
    isLoading,
    error,
    login,
    signup,
    logout,
    clearError,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
