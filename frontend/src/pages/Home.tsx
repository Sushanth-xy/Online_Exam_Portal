import { useEffect, useState } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import './Home.css';

export function Home() {
  const { user } = useAuth();
  const [healthStatus, setHealthStatus] = useState<'UP' | 'DOWN' | 'CHECKING'>('CHECKING');

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
    <div className="home-page">
      {/* Navigation */}
      <nav className="home-navbar">
        <Link className="home-brand" to="/">
          <span>🎓</span> Online Exam Portal
        </Link>
        <div className="home-nav-links">
          {user ? (
            <Link className="home-btn-primary" to="/dashboard">
              Go to Dashboard ({user.role}) →
            </Link>
          ) : (
            <>
              <Link className="home-btn-ghost" to="/login">
                Sign In
              </Link>
              <Link className="home-btn-primary" to="/register">
                Register
              </Link>
            </>
          )}
        </div>
      </nav>

      {/* Hero Header */}
      <header className="home-hero">
        <div className="home-status-badge">
          <span className="home-status-dot" />
          Backend API: {healthStatus === 'UP' ? 'Operational (UP)' : healthStatus}
        </div>

        <h1>Secure, Proctored & Automated Examination Management</h1>

        <p>
          A comprehensive platform designed for educational institutions to conduct,
          proctor, and evaluate examinations with automated question delivery, tab-switch
          incident telemetry, and audit compliance.
        </p>

        <div className="home-hero-cta">
          {user ? (
            <Link className="home-btn-primary" to="/dashboard" style={{ fontSize: '1.1rem', padding: '0.85rem 2rem' }}>
              Open My Portal Dashboard →
            </Link>
          ) : (
            <>
              <Link className="home-btn-primary" to="/login" style={{ fontSize: '1.1rem', padding: '0.85rem 2rem' }}>
                Sign In to Portal
              </Link>
              <Link className="home-btn-ghost" to="/register" style={{ fontSize: '1.1rem', padding: '0.85rem 2rem', border: '1.5px solid #cbd5e1' }}>
                Create Student Account
              </Link>
            </>
          )}
        </div>
      </header>

      {/* Role Feature Cards */}
      <section className="home-roles-grid" aria-label="Portal Roles">
        <article className="home-role-card">
          <div className="home-role-icon">🎓</div>
          <h2 className="home-role-title">Candidate & Student</h2>
          <p className="home-role-desc">
            Browse active and upcoming assessments, enter live fullscreen examination
            sessions with autosave every 15–30 seconds, and submit securely.
          </p>
          <Link className="home-role-btn" to="/student/dashboard">
            Student Portal →
          </Link>
        </article>

        <article className="home-role-card">
          <div className="home-role-icon">👨‍🏫</div>
          <h2 className="home-role-title">Instructor & Author</h2>
          <p className="home-role-desc">
            Author MCQ, True/False, and Short Answer question banks. Assemble scheduled
            exams, configure marking parameters, and monitor proctoring incidents.
          </p>
          <Link className="home-role-btn" to="/instructor/dashboard">
            Instructor Portal →
          </Link>
        </article>

        <article className="home-role-card">
          <div className="home-role-icon">🛡️</div>
          <h2 className="home-role-title">Administrator</h2>
          <p className="home-role-desc">
            Manage user lifecycles, enforce security lockouts, review real-time proctoring
            violations, and inspect system audit trail logs with 3-year retention compliance.
          </p>
          <Link className="home-role-btn" to="/admin/dashboard">
            Admin Portal →
          </Link>
        </article>
      </section>

      {/* Demo Credentials Help Box */}
      <section className="home-demo-accounts">
        <h3 className="home-demo-title">🔑 Quick Testing Credentials (Password: Password@123)</h3>
        <div className="home-demo-grid">
          <div>
            <strong>Student:</strong> <code>student@oep.edu</code>
          </div>
          <div>
            <strong>Instructor:</strong> <code>instructor@oep.edu</code>
          </div>
          <div>
            <strong>Admin:</strong> <code>admin@oep.edu</code>
          </div>
        </div>
      </section>
    </div>
  );
}
