
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth, type UserRole } from './AuthContext';

interface ProtectedRouteProps {
  allowedRoles?: UserRole[];
}

export function ProtectedRoute({
  allowedRoles,
}: ProtectedRouteProps) {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    switch (user.role) {
      case 'STUDENT':
        return <Navigate to="/student/dashboard" replace />;
      case 'INSTRUCTOR':
        return <Navigate to="/instructor/dashboard" replace />;
      case 'ADMIN':
        return <Navigate to="/admin/dashboard" replace />;
    }
  }

  return <Outlet />;
}
