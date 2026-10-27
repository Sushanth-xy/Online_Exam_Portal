import { useEffect, useState } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';

export function Home() {
  const [healthStatus, setHealthStatus] = useState<string>('CHECKING...');

  useEffect(() => {
    axios
      .get('http://localhost:8080/api/v1/health')
      .then((response) => {
        if (response.data && response.data.status === 'UP') {
          setHealthStatus('UP');
        } else {
          setHealthStatus('DOWN');
        }
      })
      .catch(() => {
        setHealthStatus('DOWN');
      });
  }, []);

  return (
    <div style={{ padding: '2rem', fontFamily: 'system-ui, sans-serif' }}>
      <h1>Online Exam Portal</h1>
      <p style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>
        Backend: <span style={{ color: healthStatus === 'UP' ? 'green' : healthStatus === 'DOWN' ? 'red' : 'orange' }}>{healthStatus}</span>
      </p>

      <nav style={{ marginTop: '2rem' }}>
        <h3>Pages</h3>
        <ul style={{ listStyle: 'none', padding: 0, display: 'flex', gap: '1rem' }}>
          <li><Link to="/login">Login</Link></li>
          <li><Link to="/student/dashboard">Student Dashboard</Link></li>
          <li><Link to="/instructor/dashboard">Instructor Dashboard</Link></li>
          <li><Link to="/admin/dashboard">Admin Dashboard</Link></li>
        </ul>
      </nav>
    </div>
  );
}
