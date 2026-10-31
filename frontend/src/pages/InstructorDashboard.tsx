import { Link } from 'react-router-dom';

export function InstructorDashboard() {
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