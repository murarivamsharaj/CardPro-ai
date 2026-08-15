import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../services/api';
import { getMyProfile } from '../services/settingsService';

interface AuthContextType {
  user: any;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (email: string, pass: string) => Promise<void>;
  signup: (data: any) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
  /** Merge fields into the cached user (e.g. pro: true after a payment). */
  updateUser: (patch: Record<string, any>) => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<any>(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const isAuthenticated = !!user;

  const clearError = () => setError(null);

  const login = async (email: string, pass: string) => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await api.post('/api/v1/auth/login', { email, password: pass });
      setUser(response.data.user);
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
      // auth-service's login payload has no `pro` field — pull the real
      // membership status from user-service so the PRO badge renders correctly.
      hydratePro();
    } catch (err: any) {
      const errorMsg =
        err.response?.data?.error?.message ||
        err.response?.data?.message ||
        'Invalid email or password';
      setError(errorMsg);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const signup = async (data: any) => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await api.post('/api/v1/auth/register', data);
      setUser(response.data.user);
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
      hydratePro();
    } catch (err: any) {
      const errorMsg =
        err.response?.data?.error?.message ||
        err.response?.data?.message ||
        'Registration failed';
      setError(errorMsg);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const updateUser = (patch: Record<string, any>) => {
    setUser((prev: any) => {
      const next = { ...prev, ...patch };
      localStorage.setItem('user', JSON.stringify(next));
      return next;
    });
  };

  /**
   * Merge the server-side profile (pro flag, etc.) into the cached auth user.
   * The auth-service login payload does not carry `pro`, so the Navbar badge
   * and any other `user.pro` consumers need this hydration from user-service.
   */
  const hydratePro = useCallback(async () => {
    try {
      const profile = await getMyProfile();
      if (profile && typeof profile.pro === 'boolean') {
        updateUser({ pro: profile.pro });
      }
    } catch {
      // user-service unreachable or profile not ready — keep the cached value.
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // On mount with an existing session, refresh pro from the server so a
  // page reload (or a payment made in a previous session) shows the badge.
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token && token !== 'undefined' && token !== 'null') {
      hydratePro();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const logout = async () => {
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  return (
    <AuthContext.Provider value={{ 
        user, 
        isAuthenticated, 
        isLoading, 
        error, 
        login, 
        signup, 
        logout, 
        clearError, 
        updateUser 
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};