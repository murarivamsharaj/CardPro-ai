import { Navigate } from 'react-router-dom'

const AdminRoute = ({ children }: { children: React.ReactNode }) => {
  // In production, check user role from auth store
  const isAdmin = true // Placeholder

  if (!isAdmin) {
    return <Navigate to="/dashboard" replace />
  }

  return <>{children}</>
}

export default AdminRoute
