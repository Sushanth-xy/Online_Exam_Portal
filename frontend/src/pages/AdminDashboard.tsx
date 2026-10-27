import { Link } from 'react-router-dom';

export function AdminDashboard() {
  return (
    <div style={{ padding: '2rem', fontFamily: 'system-ui, sans-serif' }}>
      <h2>Admin Dashboard</h2>
      <p>Placeholder page for Admin Dashboard.</p>
      <Link to="/">Back to Home</Link>
    </div>
  );
}
