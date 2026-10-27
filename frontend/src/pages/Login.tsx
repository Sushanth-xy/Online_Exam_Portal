import { Link } from 'react-router-dom';

export function Login() {
  return (
    <div style={{ padding: '2rem', fontFamily: 'system-ui, sans-serif' }}>
      <h2>Login</h2>
      <p>Placeholder page for Login.</p>
      <Link to="/">Back to Home</Link>
    </div>
  );
}
