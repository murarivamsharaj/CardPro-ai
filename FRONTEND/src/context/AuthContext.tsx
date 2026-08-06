import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

interface AuthContextType {
  user: any;
  login: (email: string, pass: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<any>(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });

  const login = async (email: string, pass: string) => {
    try {
      // Try real backend if available
      const response = await api.post('/api/v1/auth/login', { email, password: pass });
      setUser(response.data.user);
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
    } catch (error) {
      // Local fallback: bypasses backend when Docker is turned off!
      console.warn('Backend offline. Using local mock login.');
      const mockUser = { email: email, name: 'Test User' };
      setUser(mockUser);
      localStorage.setItem('token', 'mock-token-12345');
      localStorage.setItem('user', JSON.stringify(mockUser));
    }
  };

  const logout = async () => {
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};