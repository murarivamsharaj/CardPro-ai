import React from 'react';
import { ProductListPage } from './ProductListPage';

export const DashboardPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Top Navbar Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto py-4 px-6 flex justify-between items-center">
          <h1 className="text-xl font-bold text-gray-900">CardPro AI Dashboard</h1>
          <span className="text-sm text-gray-600">Welcome, Admin</span>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="py-6">
        <ProductListPage />
      </main>
    </div>
  );
};