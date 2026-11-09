import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { getExams, type Exam } from '../api/exams';
import './StudentDashboard.css';

export function StudentDashboard() {
  const { user, token, logout } = useAuth();
  const navigate = useNavigate();

  const [exams, setExams] = useState<Exam[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadExams = useCallback(async () => {
    if (!token) return;
    setLoading(true);
    setError('');

    try {
      const data = await getExams(token);
      setExams(data);
    } catch (err: unknown) {
      setError(
        err instanceof Error
          ? err.message
          : 'Unable to load scheduled exams. Please check your connection.',
      );
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    void loadExams();
  }, [loadExams]);

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  function getExamStatus(exam: Exam): {
    badgeClass: string;
    label: string;
    isAvailable: boolean;
  } {
    const now = Date.now();
    const start = new Date(exam.startTime).getTime();
    const end = new Date(exam.endTime).getTime();

    if (now < start) {
      return {
        badgeClass: 'badge-upcoming',
        label: 'Upcoming',
        isAvailable: false,
      };
    } else if (now > end) {
      return {
        badgeClass: 'badge-closed',
        label: 'Closed',
        isAvailable: false,
      };
    } else {
      return {
        badgeClass: 'badge-active',
        label: 'Active Now',
        isAvailable: true,
      };
    }
  }

  const activeCount = exams.filter((e) => getExamStatus(e).isAvailable).length;

  return (
    <div className="student-dashboard-page">
      {/* Top Navigation */}
      <nav className="student-navbar">
        <Link className="student-navbar-brand" to="/student/dashboard">
          <span>🎓</span> Online Exam Portal
        </Link>
        <div className="student-navbar-actions">
          <div className="student-user-pill">
            <span>👤</span>
            <strong>{user?.name || 'Student'}</strong>
            <span className="student-role-badge">{user?.role || 'STUDENT'}</span>
          </div>
          <button
            type="button"
            className="student-logout-btn"
            onClick={handleLogout}
          >
            Log Out
          </button>
        </div>
      </nav>

      <main className="student-container">
        {/* Hero Section */}
        <header className="student-hero">
          <h1>Welcome, {user?.name || 'Student'}! 👋</h1>
          <p>
            Welcome to your examination hub. Review your scheduled examinations,
            monitor timing windows, and launch timed sessions with automated proctoring.
          </p>
          <div className="student-stats-row">
            <div className="student-stat-card">
              <div className="student-stat-number">{exams.length}</div>
              <div className="student-stat-label">Total Assigned Exams</div>
            </div>
            <div className="student-stat-card">
              <div className="student-stat-number">{activeCount}</div>
              <div className="student-stat-label">Active / Ready to Take</div>
            </div>
            <div className="student-stat-card">
              <div className="student-stat-number">30s</div>
              <div className="student-stat-label">Live Auto-Save Sync</div>
            </div>
            <div className="student-stat-card">
              <div className="student-stat-number">🛡️ Active</div>
              <div className="student-stat-label">Fullscreen Proctoring</div>
            </div>
          </div>
        </header>

        {/* Available Exams Section */}
        <section aria-labelledby="exams-heading">
          <div className="student-section-header">
            <h2 id="exams-heading" className="student-section-title">
              Available Examinations
            </h2>
            <button
              type="button"
              className="student-refresh-btn"
              onClick={() => void loadExams()}
              disabled={loading}
            >
              {loading ? 'Refreshing...' : '🔄 Refresh List'}
            </button>
          </div>

          {error && (
            <div
              style={{
                backgroundColor: '#fee2e2',
                color: '#b91c1c',
                padding: '1rem',
                borderRadius: '8px',
                marginBottom: '1.5rem',
              }}
            >
              ⚠️ {error}
            </div>
          )}

          {loading && exams.length === 0 ? (
            <div className="student-empty-state">
              <p>Loading your examinations...</p>
            </div>
          ) : exams.length === 0 ? (
            <div className="student-empty-state">
              <h3>No examinations found</h3>
              <p>There are no exams currently scheduled for your account.</p>
            </div>
          ) : (
            <div className="student-exams-grid">
              {exams.map((exam) => {
                const status = getExamStatus(exam);
                const questionCount =
                  exam.questions?.length ?? exam.questionIds?.length ?? '—';

                return (
                  <article key={exam.id} className="student-exam-card">
                    <div className="student-exam-card-header">
                      <h3 className="student-exam-card-title">{exam.title}</h3>
                      <span className={status.badgeClass}>{status.label}</span>
                    </div>

                    <p className="student-exam-card-desc">
                      {exam.description || 'No description provided for this assessment.'}
                    </p>

                    <div className="student-exam-meta-grid">
                      <div className="student-meta-item">
                        <span className="student-meta-label">Duration</span>
                        <span className="student-meta-value">
                          ⏱️ {exam.durationMinutes} min
                        </span>
                      </div>
                      <div className="student-meta-item">
                        <span className="student-meta-label">Questions</span>
                        <span className="student-meta-value">
                          📝 {questionCount} questions
                        </span>
                      </div>
                      <div className="student-meta-item">
                        <span className="student-meta-label">Pass Score</span>
                        <span className="student-meta-value">
                          🎯 {exam.passPercentage}%
                        </span>
                      </div>
                      <div className="student-meta-item">
                        <span className="student-meta-label">Negative Marking</span>
                        <span className="student-meta-value">
                          ⚖️{' '}
                          {exam.negativeMarkingFactor > 0
                            ? `-${exam.negativeMarkingFactor * 100}%`
                            : 'None'}
                        </span>
                      </div>
                      <div
                        className="student-meta-item"
                        style={{ gridColumn: 'span 2' }}
                      >
                        <span className="student-meta-label">Window Closes</span>
                        <span className="student-meta-value">
                          📅 {new Date(exam.endTime).toLocaleString()}
                        </span>
                      </div>
                    </div>

                    <div className="student-card-actions">
                      {status.isAvailable ? (
                        <>
                          <Link
                            to={`/student/exams/${exam.id}`}
                            className="btn-start-exam"
                          >
                            🚀 Take Exam (Proctored)
                          </Link>
                          <Link
                            to={`/student/exams/${exam.id}/taking`}
                            className="btn-secondary-exam"
                            title="Question-palette exam taking view"
                          >
                            Standard View
                          </Link>
                        </>
                      ) : (
                        <button
                          type="button"
                          className="btn-start-exam"
                          disabled
                        >
                          {status.label === 'Upcoming'
                            ? '⏳ Opens at ' + new Date(exam.startTime).toLocaleTimeString()
                            : '🔒 Exam Closed'}
                        </button>
                      )}
                    </div>
                  </article>
                );
              })}
            </div>
          )}
        </section>

        {/* Proctoring Instructions & Guidelines */}
        <section className="student-info-card">
          <h3 className="student-info-title">
            🛡️ Important Examination & Proctoring Guidelines
          </h3>
          <ul className="student-info-list">
            <li>
              <strong>Fullscreen Enforcement:</strong> All proctored examinations require
              active fullscreen mode. Exiting fullscreen triggers an automated violation alert.
            </li>
            <li>
              <strong>Tab Switch Telemetry:</strong> Switching browser tabs, minimizing the
              window, or navigating away records a proctoring incident for instructor review.
            </li>
            <li>
              <strong>Live Autosave:</strong> Your selected answers are continuously synchronized
              to the backend every 15 to 30 seconds.
            </li>
            <li>
              <strong>Countdown Expiration:</strong> When the timer reaches 00:00, your current
              answers will be automatically submitted and locked.
            </li>
          </ul>
        </section>
      </main>
    </div>
  );
}
