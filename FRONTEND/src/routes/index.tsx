import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { LoginPage } from '../pages/auth/LoginPage';
import { RegisterPage } from '../pages/auth/RegisterPage';
import { DashboardPage } from '../pages/dashboard/DashboardPage'; 
import { CreateCardPage } from '../pages/dashboard/CreateCardPage'; // 👈 1. Import your CreateCardPage component (adjust path if needed)
import PrivateRoute from './PrivateRoute'; 

export const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      
      <Route path="/dashboard" element={
        <PrivateRoute>
          <DashboardPage />
        </PrivateRoute>
      } />

      {/* 👇 2. Add the Create Card route protected by PrivateRoute */}
      <Route path="/create-card" element={
        <PrivateRoute>
          <CreateCardPage />
        </PrivateRoute>
      } />
    </Routes>
  );
};