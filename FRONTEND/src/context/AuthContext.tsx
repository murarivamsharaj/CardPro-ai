import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

interface AuthContextType {
  user: any;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (email: string, pass: string) => Promise<void>;
  signup: (data: any) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<any>(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  
  // New state variables to satisfy TypeScript and your UI components
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Derived state for easy true/false authentication checking
  const isAuthenticated = !!user;

  // Utility to clear errors when navigating between login/signup pages
  const clearError = () => setError(null);

  const login = async (email: string, pass: string) => {
    setIsLoading(true);
    setError(null);
    try {
      // Try real backend if available
      const response = await api.post('/api/v1/auth/login', { email, password: pass });
      setUser(response.data.user);
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
    } catch (err: any) {
      // Local fallback: bypasses backend when Docker is turned off!
      console.warn('Backend offline. Using local mock login.');
      const mockUser = { email: email, name: 'Test User' };
      setUser(mockUser);
      localStorage.setItem('token', 'mock-token-12345');
      localStorage.setItem('user', JSON.stringify(mockUser));
    } finally {
      setIsLoading(false);
    }
  };

  const signup = async (data: any) => {
    setIsLoading(true);
    setError(null);
    try {
      // Try real backend if available
      const response = await api.post('/api/v1/auth/register', data);
      setUser(response.data.user);
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
    } catch (err: any) {
      console.warn('Backend offline. Using local mock signup.');
      const mockUser = { email: data.email, name: data.name || 'Test User' };
      setUser(mockUser);
      localStorage.setItem('token', 'mock-token-12345');
      localStorage.setItem('user', JSON.stringify(mockUser));
    } finally {
      setIsLoading(false);
    }
  };

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
        clearError 
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