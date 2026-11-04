import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import {
  getInstructorProctoringIncidents,
  type ProctoringIncident,
} from '../api/proctoring';

export function InstructorDashboard() {
  const { token } = useAuth();
  const [incidents, setIncidents] = useState<ProctoringIncident[]>([]);
  const [incidentsLoading, setIncidentsLoading] = useState(false);
  const [incidentError, setIncidentError] = useState('');

  const loadIncidents = useCallback(async () => {
    if (!token) return;

    setIncidentsLoading(true);
    setIncidentError('');

    try {
      setIncidents(await getInstructorProctoringIncidents(token));
    } catch (error) {
      setIncidentError(
        error instanceof Error
          ? error.message
          : 'Unable to load proctoring incidents.',
      );
    } finally {
      setIncidentsLoading(false);
    }
  }, [token]);

  useEffect(() => {
    void loadIncidents();
  }, [loadIncidents]);

  return (
    <main style={styles.page}>
      <div style={styles.container}>
        <header style={styles.header}>
          <div>
            <h1 style={styles.heading}>Instructor Dashboard</h1>
            <p style={styles.subtitle}>
              Manage your question bank and create exams.
            </p>
          </div>
        </header>

        <section style={styles.grid}>
          <Link
            to="/instructor/questions/new"
            style={styles.card}
          >
            <div style={styles.icon}>📝</div>

            <h2 style={styles.cardTitle}>Create Question</h2>

            <p style={styles.cardDescription}>
              Add Multiple Choice, True/False, and Short Answer
              questions to your question bank.
            </p>

            <span style={styles.cardLink}>
              Open question creator →
            </span>
          </Link>

          <Link
            to="/instructor/exams/new"
            style={styles.card}
          >
            <div style={styles.icon}>📋</div>

            <h2 style={styles.cardTitle}>Create Exam</h2>

            <p style={styles.cardDescription}>
              Assemble questions into an exam, configure its duration,
              schedule, and marking settings.
            </p>

            <span style={styles.cardLink}>
              Open exam creator →
            </span>
          </Link>
        </section>

        <section style={styles.monitoring}>
          <div style={styles.monitoringHeading}>
            <div>
              <h2 style={styles.monitoringTitle}>Proctoring Incidents</h2>
              <p style={styles.subtitle}>
                Review incidents from attempts for your exams.
              </p>
            </div>
            <button
              type="button"
              onClick={() => void loadIncidents()}
              disabled={incidentsLoading}
            >
              {incidentsLoading ? 'Refreshing...' : 'Refresh incidents'}
            </button>
          </div>

          {incidentError && <p role="alert">{incidentError}</p>}
          {incidentsLoading && incidents.length === 0 ? (
            <p>Loading incidents...</p>
          ) : incidents.length === 0 ? (
            <p>No proctoring incidents have been recorded.</p>
          ) : (
            <div style={styles.tableWrapper}>
              <table style={styles.table}>
                <thead>
                  <tr>
                    <th>Timestamp</th>
                    <th>Candidate</th>
                    <th>Exam</th>
                    <th>Attempt ID</th>
                    <th>Incident</th>
                  </tr>
                </thead>
                <tbody>
                  {incidents.map((incident) => (
                    <tr key={incident.id}>
                      <td>{new Date(incident.occurredAt).toLocaleString()}</td>
                      <td>{incident.studentEmail}</td>
                      <td>{incident.examTitle}</td>
                      <td>{incident.attemptId}</td>
                      <td>
                        {incident.type === 'TAB_SWITCH'
                          ? 'Tab Switch'
                          : 'Fullscreen Exit'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <footer style={styles.footer}>
          <Link to="/" style={styles.backLink}>
            ← Back to Home
          </Link>
        </footer>
      </div>
    </main>
  );
}

const styles: Record<string, React.CSSProperties> = {
  page: {
    minHeight: '100vh',
    padding: '48px 24px',
    background: '#f3f6fc',
    fontFamily: 'system-ui, sans-serif',
  },

  container: {
    maxWidth: '1000px',
    margin: '0 auto',
  },

  header: {
    marginBottom: '32px',
  },

  heading: {
    margin: 0,
    color: '#172033',
    fontSize: '30px',
    fontWeight: 750,
  },

  subtitle: {
    marginTop: '10px',
    color: '#697386',
    fontSize: '16px',
  },

  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
    gap: '24px',
  },

  monitoring: {
    marginTop: '36px',
    padding: '24px',
    background: '#fff',
    border: '1px solid #e6eaf2',
    borderRadius: '16px',
  },

  monitoringHeading: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: '16px',
    marginBottom: '16px',
  },

  monitoringTitle: {
    margin: 0,
    color: '#172033',
    fontSize: '21px',
  },

  tableWrapper: {
    overflowX: 'auto',
  },

  table: {
    width: '100%',
    borderCollapse: 'collapse',
    textAlign: 'left',
  },

  card: {
    display: 'block',
    padding: '28px',
    background: '#fff',
    border: '1px solid #e6eaf2',
    borderRadius: '16px',
    color: '#172033',
    textDecoration: 'none',
    boxShadow: '0 10px 30px rgb(31 41 55 / 5%)',
    transition: 'transform 0.2s ease, box-shadow 0.2s ease',
  },

  icon: {
    fontSize: '32px',
    marginBottom: '16px',
  },

  cardTitle: {
    margin: '0 0 12px',
    fontSize: '21px',
    fontWeight: 700,
  },

  cardDescription: {
    minHeight: '72px',
    margin: '0 0 20px',
    color: '#697386',
    fontSize: '14px',
    lineHeight: 1.6,
  },

  cardLink: {
    color: '#4f46e5',
    fontSize: '14px',
    fontWeight: 700,
  },

  footer: {
    marginTop: '36px',
  },

  backLink: {
    color: '#4f46e5',
    fontWeight: 600,
    textDecoration: 'none',
  },
};