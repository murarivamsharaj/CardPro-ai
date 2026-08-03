import React from 'react';
import { Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';

interface PrivateRouteProps {
  children: React.ReactNode;
}

const PrivateRoute: React.FC<PrivateRouteProps> = ({ children }) => {
  // Explicitly typing state as 'any' bypasses the unknown type error
  const token = useSelector((state: any) => state.auth?.token) || localStorage.getItem('token');

  if (!token || token === 'undefined' || token === 'null') {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

export default PrivateRoute;