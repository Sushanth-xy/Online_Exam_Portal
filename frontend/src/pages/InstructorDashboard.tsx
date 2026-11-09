import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import {
  getInstructorProctoringIncidents,
  type ProctoringIncident,
} from '../api/proctoring';
import {
  getSubmissions,
  gradeSubmission,
  type GradingSubmissionDTO,
} from '../api/grading';

export function InstructorDashboard() {
  const { user, token, logout } = useAuth();
  const navigate = useNavigate();

  // Proctoring state
  const [incidents, setIncidents] = useState<ProctoringIncident[]>([]);
  const [incidentsLoading, setIncidentsLoading] = useState(false);
  const [incidentError, setIncidentError] = useState('');

  // Grading state
  const [submissions, setSubmissions] = useState<GradingSubmissionDTO[]>([]);
  const [submissionsLoading, setSubmissionsLoading] = useState(false);
  const [gradingError, setGradingError] = useState('');
  const [gradingSuccess, setGradingSuccess] = useState('');
  const [activeSubmission, setActiveSubmission] = useState<GradingSubmissionDTO | null>(null);
  const [gradeInputs, setGradeInputs] = useState<Record<number, { marks: number; feedback: string }>>({});
  const [submittingGrade, setSubmittingGrade] = useState(false);

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

  const loadSubmissions = useCallback(async () => {
    if (!token) return;
    setSubmissionsLoading(true);
    setGradingError('');

    try {
      const data = await getSubmissions(token);
      setSubmissions(data);
    } catch (error) {
      setGradingError(
        error instanceof Error
          ? error.message
          : 'Unable to load student submissions.',
      );
    } finally {
      setSubmissionsLoading(false);
    }
  }, [token]);

  useEffect(() => {
    void loadIncidents();
    void loadSubmissions();
  }, [loadIncidents, loadSubmissions]);

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  function openGradingModal(sub: GradingSubmissionDTO) {
    setActiveSubmission(sub);
    setGradingError('');
    setGradingSuccess('');
    const initialInputs: Record<number, { marks: number; feedback: string }> = {};
    for (const ans of sub.answers) {
      initialInputs[ans.questionId] = {
        marks: ans.awardedMarks ?? 0,
        feedback: ans.feedback ?? '',
      };
    }
    setGradeInputs(initialInputs);
  }

  async function handleSaveGrades() {
    if (!token || !activeSubmission) return;
    setSubmittingGrade(true);
    setGradingError('');
    setGradingSuccess('');

    try {
      const gradePayload = Object.entries(gradeInputs).map(([qid, val]) => ({
        questionId: Number(qid),
        awardedMarks: Number(val.marks),
        feedback: val.feedback,
      }));

      const updated = await gradeSubmission(token, activeSubmission.attemptId, gradePayload);
      setGradingSuccess(`Grades successfully published! New score: ${updated.totalScore}`);
      setActiveSubmission(null);
      void loadSubmissions();
    } catch (err) {
      setGradingError(
        err instanceof Error
          ? err.message
          : 'Failed to save grades. Please check mark values.',
      );
    } finally {
      setSubmittingGrade(false);
    }
  }

  return (
    <main style={styles.page}>
      <div style={styles.container}>
        <header style={styles.header}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <h1 style={styles.heading}>Instructor Dashboard</h1>
              <p style={styles.subtitle}>
                Author questions, assemble exams, grade submissions, and monitor proctoring.
              </p>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <div style={{ padding: '0.4rem 0.8rem', background: '#e0e7ff', borderRadius: '20px', fontSize: '0.875rem', color: '#3730a3', fontWeight: 600 }}>
                👨‍🏫 {user?.name || 'Instructor'}
              </div>
              <button
                type="button"
                onClick={handleLogout}
                style={{ padding: '0.4rem 0.9rem', background: 'transparent', border: '1px solid #fecaca', color: '#dc2626', borderRadius: '6px', cursor: 'pointer', fontWeight: 600 }}
              >
                Log Out
              </button>
            </div>
          </div>
        </header>

        <section style={styles.grid}>
          <Link to="/instructor/questions/new" style={styles.card}>
            <div style={styles.icon}>📝</div>
            <h2 style={styles.cardTitle}>Create Question</h2>
            <p style={styles.cardDescription}>
              Add Multiple Choice, True/False, and Short Answer questions to your question bank.
            </p>
            <span style={styles.cardLink}>Open question creator →</span>
          </Link>

          <Link to="/instructor/exams/new" style={styles.card}>
            <div style={styles.icon}>📋</div>
            <h2 style={styles.cardTitle}>Create Exam</h2>
            <p style={styles.cardDescription}>
              Assemble questions into an exam, configure its duration, schedule, and marking settings.
            </p>
            <span style={styles.cardLink}>Open exam creator →</span>
          </Link>
        </section>

        {/* Submissions & Manual Grading Section */}
        <section style={styles.monitoring}>
          <div style={styles.monitoringHeading}>
            <div>
              <h2 style={styles.monitoringTitle}>Student Submissions & Grading</h2>
              <p style={styles.subtitle}>
                Review student exam attempts, grade subjective responses, and view total scores.
              </p>
            </div>
            <button
              type="button"
              onClick={() => void loadSubmissions()}
              disabled={submissionsLoading}
              style={styles.refreshBtn}
            >
              {submissionsLoading ? 'Refreshing...' : '🔄 Refresh submissions'}
            </button>
          </div>

          {gradingError && <p role="alert" style={{ color: '#dc2626', fontWeight: 600 }}>{gradingError}</p>}
          {gradingSuccess && <p role="status" style={{ color: '#16a34a', fontWeight: 600 }}>{gradingSuccess}</p>}

          {submissionsLoading && submissions.length === 0 ? (
            <p>Loading submissions...</p>
          ) : submissions.length === 0 ? (
            <p style={{ color: '#64748b' }}>No student exam submissions have been completed yet.</p>
          ) : (
            <div style={styles.tableWrapper}>
              <table style={styles.table}>
                <thead>
                  <tr>
                    <th>Attempt ID</th>
                    <th>Candidate</th>
                    <th>Exam</th>
                    <th>Total Score</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {submissions.map((sub) => (
                    <tr key={sub.attemptId} style={{ borderBottom: '1px solid #f1f5f9' }}>
                      <td style={{ padding: '12px 8px' }}>#{sub.attemptId}</td>
                      <td style={{ padding: '12px 8px' }}>{sub.studentEmail}</td>
                      <td style={{ padding: '12px 8px' }}>{sub.examTitle}</td>
                      <td style={{ padding: '12px 8px', fontWeight: 700 }}>{sub.totalScore} pts</td>
                      <td style={{ padding: '12px 8px' }}>
                        <span
                          style={{
                            padding: '4px 10px',
                            borderRadius: '12px',
                            fontSize: '0.8rem',
                            fontWeight: 700,
                            background: sub.allSubjectiveEvaluated ? '#dcfce7' : '#fef9c3',
                            color: sub.allSubjectiveEvaluated ? '#15803d' : '#854d0e',
                          }}
                        >
                          {sub.allSubjectiveEvaluated ? 'Evaluated' : 'Needs Grading'}
                        </span>
                      </td>
                      <td style={{ padding: '12px 8px' }}>
                        <button
                          type="button"
                          onClick={() => openGradingModal(sub)}
                          style={{
                            background: '#4f46e5',
                            color: '#fff',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '6px',
                            fontSize: '0.85rem',
                            fontWeight: 600,
                            cursor: 'pointer',
                          }}
                        >
                          Grade / Details
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        {/* Modal for Grading Submission */}
        {activeSubmission && (
          <div
            style={{
              position: 'fixed',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              backgroundColor: 'rgba(15, 23, 42, 0.6)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              zIndex: 100,
              padding: '1rem',
            }}
          >
            <div
              style={{
                background: '#ffffff',
                borderRadius: '16px',
                maxWidth: '750px',
                width: '100%',
                maxHeight: '90vh',
                overflowY: 'auto',
                padding: '2rem',
                boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <div>
                  <h2 style={{ margin: 0, fontSize: '1.4rem', color: '#0f172a' }}>
                    Grading: {activeSubmission.examTitle}
                  </h2>
                  <p style={{ margin: '4px 0 0 0', color: '#64748b', fontSize: '0.9rem' }}>
                    Candidate: <strong>{activeSubmission.studentEmail}</strong> (Attempt #{activeSubmission.attemptId})
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() => setActiveSubmission(null)}
                  style={{ background: 'transparent', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: '#64748b' }}
                >
                  ✕
                </button>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', marginBottom: '2rem' }}>
                {activeSubmission.answers.map((ans, idx) => (
                  <div
                    key={ans.questionId}
                    style={{
                      border: '1px solid #e2e8f0',
                      borderRadius: '10px',
                      padding: '1.25rem',
                      background: ans.autoGraded ? '#f8fafc' : '#ffffff',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                      <strong style={{ color: '#0f172a' }}>
                        Question {idx + 1} ({ans.questionType})
                      </strong>
                      <span style={{ fontSize: '0.85rem', color: '#64748b', fontWeight: 600 }}>
                        Max Marks: {ans.maxMarks}
                      </span>
                    </div>

                    <p style={{ margin: '0 0 0.75rem 0', color: '#334155', fontSize: '0.95rem' }}>
                      {ans.questionPrompt}
                    </p>

                    <div style={{ background: '#f1f5f9', padding: '0.75rem', borderRadius: '6px', marginBottom: '1rem' }}>
                      <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: '#64748b', fontWeight: 700, display: 'block', marginBottom: '2px' }}>
                        Candidate Answer:
                      </span>
                      <span style={{ color: '#0f172a', fontWeight: 600 }}>
                        {ans.selectedOption || 'No answer submitted'}
                      </span>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1rem' }}>
                      <div>
                        <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, color: '#475569', marginBottom: '4px' }}>
                          Awarded Marks (0 – {ans.maxMarks})
                        </label>
                        <input
                          type="number"
                          min="0"
                          max={ans.maxMarks}
                          step="0.5"
                          value={gradeInputs[ans.questionId]?.marks ?? 0}
                          onChange={(e) =>
                            setGradeInputs((prev) => ({
                              ...prev,
                              [ans.questionId]: {
                                ...prev[ans.questionId],
                                marks: parseFloat(e.target.value) || 0,
                                feedback: prev[ans.questionId]?.feedback || '',
                              },
                            }))
                          }
                          style={{
                            width: '100%',
                            padding: '8px',
                            border: '1px solid #cbd5e1',
                            borderRadius: '6px',
                            fontSize: '0.95rem',
                            fontWeight: 600,
                          }}
                        />
                      </div>
                      <div>
                        <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, color: '#475569', marginBottom: '4px' }}>
                          Feedback / Remarks
                        </label>
                        <input
                          type="text"
                          placeholder="e.g. Well answered!"
                          value={gradeInputs[ans.questionId]?.feedback ?? ''}
                          onChange={(e) =>
                            setGradeInputs((prev) => ({
                              ...prev,
                              [ans.questionId]: {
                                ...prev[ans.questionId],
                                marks: prev[ans.questionId]?.marks || 0,
                                feedback: e.target.value,
                              },
                            }))
                          }
                          style={{
                            width: '100%',
                            padding: '8px',
                            border: '1px solid #cbd5e1',
                            borderRadius: '6px',
                            fontSize: '0.95rem',
                          }}
                        />
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
                <button
                  type="button"
                  onClick={() => setActiveSubmission(null)}
                  style={{
                    padding: '0.6rem 1.25rem',
                    background: '#ffffff',
                    border: '1px solid #cbd5e1',
                    borderRadius: '8px',
                    fontWeight: 600,
                    cursor: 'pointer',
                  }}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={() => void handleSaveGrades()}
                  disabled={submittingGrade}
                  style={{
                    padding: '0.6rem 1.5rem',
                    background: '#2563eb',
                    border: 'none',
                    borderRadius: '8px',
                    color: '#ffffff',
                    fontWeight: 700,
                    cursor: 'pointer',
                  }}
                >
                  {submittingGrade ? 'Publishing...' : 'Publish Grades & Save'}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Proctoring Incidents Section */}
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
              style={styles.refreshBtn}
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

  refreshBtn: {
    padding: '0.45rem 1rem',
    background: '#f8fafc',
    border: '1px solid #cbd5e1',
    borderRadius: '6px',
    fontWeight: 600,
    cursor: 'pointer',
    color: '#334155',
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
