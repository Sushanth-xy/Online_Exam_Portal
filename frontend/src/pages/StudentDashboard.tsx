
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function StudentDashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <div style={{ padding: '2rem', fontFamily: 'system-ui, sans-serif' }}>
      <h2>Student Dashboard</h2>
      <p>Welcome, {user?.name || 'Student'}!</p>
      <p>You are signed in as a student.</p>

      <button type="button" onClick={handleLogout}>
        Log out
      </button>

      <p>
        <Link to="/">Back to Home</Link>
      </p>
    </div>
  );
}
