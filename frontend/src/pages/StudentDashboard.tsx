import { Link } from 'react-router-dom';

export function StudentDashboard() {
  return (
    <div style={{ padding: '2rem', fontFamily: 'system-ui, sans-serif' }}>
      <h2>Student Dashboard</h2>
      <p>Placeholder page for Student Dashboard.</p>
      <Link to="/">Back to Home</Link>
    </div>
  );
}
