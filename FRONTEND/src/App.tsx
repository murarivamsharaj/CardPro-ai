import { Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './routes/PrivateRoute';
import AdminRoute from './routes/AdminRoute';
import AppLayout from './layouts/AppLayout';
import SignupPage from './pages/auth/SignupPage';
import { LoginPage } from './pages/auth/LoginPage'; 
import DashboardPage from './pages/dashboard/DashboardPage';
import AnalyticsPage from './pages/dashboard/AnalyticsPage';
import CreateCardPage from './pages/dashboard/CreateCardPage';
import LeadsPage from './pages/dashboard/LeadsPage';
import PublicCardViewer from './pages/public/PublicCardViewer';
import { DynamicIsland } from './components/common/DynamicIsland';
import { ROUTES } from './utils/constants';

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* ── Public Routes ── */}
        <Route path={ROUTES.LOGIN} element={<LoginPage />} />
        <Route path={ROUTES.SIGNUP} element={<SignupPage />} />

        {/* Public digital card viewer — intentionally outside any auth guard so
            anyone with the link can view the profile without logging in. */}
        <Route path="/c/:slug" element={<PublicCardViewer />} />

        {/* Alias: If you type /register, it safely redirects to your actual signup route */}
        <Route path="/register" element={<Navigate to={ROUTES.SIGNUP} replace />} />

        {/* ── Protected Routes (any authenticated user) ── */}
        <Route
          path="/dashboard"
          element={
            <PrivateRoute>
              <AppLayout />
            </PrivateRoute>
          }
        >
          <Route index element={<DashboardPage />} />
          <Route path="cards" element={<PlaceholderPage title="My Cards" />} />
          <Route path="cards/create" element={<CreateCardPage />} />
          <Route path="leads" element={<LeadsPage />} />
          <Route path="analytics" element={<AnalyticsPage />} />
          <Route path="store" element={<PlaceholderPage title="Store" />} />
          <Route path="settings" element={<PlaceholderPage title="Settings" />} />
        </Route>

        {/* ── Admin Routes ── */}
        <Route
          path="/admin"
          element={
            <AdminRoute>
              <AppLayout />
            </AdminRoute>
          }
        >
          <Route index element={<PlaceholderPage title="Admin Dashboard" />} />
          <Route path="users" element={<PlaceholderPage title="User Management" />} />
          <Route path="transactions" element={<PlaceholderPage title="Transactions" />} />
        </Route>

        {/* ── Catch-all redirect ── */}
        <Route path="*" element={<Navigate to={ROUTES.LOGIN} replace />} />
      </Routes>

      {/* Dynamic Island notifications (top-center pill) */}
      <DynamicIsland />

      {/* Global toast notifications */}
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            background: '#1f2937',
            color: '#f9fafb',
            borderRadius: '0.75rem',
            fontSize: '0.875rem',
          },
          success: {
            iconTheme: { primary: '#10b981', secondary: '#f9fafb' },
          },
          error: {
            iconTheme: { primary: '#ef4444', secondary: '#f9fafb' },
          },
        }}
      />
    </AuthProvider>
  );
}

function PlaceholderPage({ title }: { title: string }) {
  return (
    <div className="flex flex-col items-center justify-center py-20">
      <div className="glass-panel p-4 text-fuchsia-300">
        <svg className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
        </svg>
      </div>
      <h2 className="mt-4 text-xl font-semibold text-white">{title}</h2>
      <p className="mt-1 text-sm text-white/50">This section is coming soon.</p>
    </div>
  );
}