
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import { AuthProvider, useAuth } from './auth/AuthContext';
import { ProtectedRoute } from './auth/ProtectedRoute';

import { Home } from './pages/Home';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { StudentDashboard } from './pages/StudentDashboard';
import { InstructorDashboard } from './pages/InstructorDashboard';
import { AdminDashboard } from './pages/AdminDashboard';
import { QuestionCreator } from './pages/QuestionCreator';
import { ExamCreator } from './pages/ExamCreator';
import { ExamTaking } from './pages/ExamTaking';

import './App.css';

function DashboardRedirect() {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  switch (user.role) {
    case 'STUDENT':
      return <Navigate to="/student/dashboard" replace />;

    case 'INSTRUCTOR':
      return <Navigate to="/instructor/dashboard" replace />;

    case 'ADMIN':
      return <Navigate to="/admin/dashboard" replace />;

    default:
      return <Navigate to="/login" replace />;
  }
}

function AppRoutes() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Student routes */}
      <Route element={<ProtectedRoute allowedRoles={['STUDENT']} />}>
        <Route
          path="/student/dashboard"
          element={<StudentDashboard />}
        />

        <Route
          path="/student/exams/:examId"
          element={<ExamTaking />}
        />
      </Route>

      {/* Instructor and Admin routes */}
      <Route
        element={
          <ProtectedRoute allowedRoles={['INSTRUCTOR', 'ADMIN']} />
        }
      >
        <Route
          path="/instructor/dashboard"
          element={<InstructorDashboard />}
        />

        <Route
          path="/instructor/questions/new"
          element={<QuestionCreator />}
        />

        <Route
          path="/instructor/exams/new"
          element={<ExamCreator />}
        />
      </Route>

      {/* Admin routes */}
      <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
        <Route
          path="/admin/dashboard"
          element={<AdminDashboard />}
        />
      </Route>

      {/* Dashboard redirect */}
      <Route path="/dashboard" element={<DashboardRedirect />} />

      {/* Unknown routes */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
