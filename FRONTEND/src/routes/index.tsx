import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { LoginPage } from '../pages/auth/LoginPage';
import { RegisterPage } from '../pages/auth/RegisterPage';

// ✅ FIXED: Point to the exact file name shown in your explorer
import { DashboardPage } from '../pages/dashboard/DashboardPage'; 

import PrivateRoute from './PrivateRoute'; 

export const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      
      <Route path="/dashboard" element={
        <PrivateRoute>
          {/* ✅ FIXED: Use the correct component name */}
          <DashboardPage />
        </PrivateRoute>
      } />
    </Routes>
  );
};