
import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import {
  autosaveExamAnswers,
  getActiveAttempt,
  reportProctoringIncident,
  startExam,
  submitExam,
  type CandidateExam,
} from '../api/exams';

type IncidentType = 'TAB_SWITCH' | 'FULLSCREEN_EXIT';

export function StudentExam() {
  const { examId: examIdParam } = useParams();
  const examId = Number(examIdParam);
  const { token } = useAuth();
  const navigate = useNavigate();

  const [exam, setExam] = useState<CandidateExam | null>(null);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [remainingSeconds, setRemainingSeconds] = useState(0);
  const [incidentCounts, setIncidentCounts] = useState({
    TAB_SWITCH: 0,
    FULLSCREEN_EXIT: 0,
  });

  const answersRef = useRef<Record<string, string>>({});
  const attemptIdRef = useRef<number | null>(null);
  const reportedEventsRef = useRef<Set<string>>(new Set());
  const submitStartedRef = useRef(false);
  const lastSaveTimeRef = useRef(0);

  useEffect(() => {
    answersRef.current = answers;
  }, [answers]);

  const saveAnswers = useCallback(async () => {
    if (!token || !exam || submitting || submitStartedRef.current) {
      return;
    }

    const answerList = Object.entries(answersRef.current).map(
      ([questionId, selectedOption]) => ({
        questionId: Number(questionId),
        selectedOption,
      }),
    );

    try {
      await autosaveExamAnswers(token, exam.examId, answerList);
      lastSaveTimeRef.current = Date.now();
      setNotice('Answers saved.');
    } catch {
      setNotice('Unable to save answers. Please check your connection.');
    }
  }, [token, exam, submitting]);

  const reportIncident = useCallback(
    async (type: IncidentType) => {
      if (!token || !attemptIdRef.current || submitStartedRef.current) {
        return;
      }

      // Prevent duplicate reports for the same browser event.
      const eventKey = `${type}:${Date.now()}:${reportedEventsRef.current.size}`;

      if (reportedEventsRef.current.has(eventKey)) {
        return;
      }

      reportedEventsRef.current.add(eventKey);

      setIncidentCounts((previous) => ({
        ...previous,
        [type]: previous[type] + 1,
      }));

      try {
        await reportProctoringIncident(
          token,
          attemptIdRef.current,
          type,
        );
      } catch {
        setNotice(
          'A proctoring incident could not be sent to the server. Check your connection.',
        );
      }
    },
    [token],
  );

  useEffect(() => {
    let cancelled = false;

    async function loadExam() {
      if (!token || !Number.isInteger(examId) || examId <= 0) {
        setError('Invalid exam link or missing login session.');
        setLoading(false);
        return;
      }

      try {
        let candidateExam: CandidateExam;

        try {
          candidateExam = await getActiveAttempt(token, examId);
        } catch {
          candidateExam = await startExam(token, examId);
        }

        if (cancelled) return;

        setExam(candidateExam);
        attemptIdRef.current = candidateExam.attemptId;
        setAnswers(candidateExam.savedAnswers ?? {});
        answersRef.current = candidateExam.savedAnswers ?? {};
        setRemainingSeconds(candidateExam.remainingSeconds);
      } catch (requestError: unknown) {
        if (cancelled) return;

        const message =
          requestError instanceof Error
            ? requestError.message
            : 'Unable to load this exam.';

        setError(
          `${message} The exam may not be available yet, or the attempt may already be completed.`,
        );
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    void loadExam();

    return () => {
      cancelled = true;
    };
  }, [token, examId]);

  // Record when the candidate switches away from the exam tab.
  useEffect(() => {
    if (!exam || exam.status !== 'IN_PROGRESS') return;

    function handleVisibilityChange() {
      if (document.visibilityState === 'hidden') {
        void reportIncident('TAB_SWITCH');
      }
    }

    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      document.removeEventListener(
        'visibilitychange',
        handleVisibilityChange,
      );
    };
  }, [exam, reportIncident]);

  // Record when the candidate exits fullscreen while the exam is active.
  useEffect(() => {
    if (!exam || exam.status !== 'IN_PROGRESS') return;

    function handleFullscreenChange() {
      if (!document.fullscreenElement) {
        void reportIncident('FULLSCREEN_EXIT');
      }
    }

    document.addEventListener(
      'fullscreenchange',
      handleFullscreenChange,
    );

    return () => {
      document.removeEventListener(
        'fullscreenchange',
        handleFullscreenChange,
      );
    };
  }, [exam, reportIncident]);

  // Countdown timer; auto-submit when time expires.
  useEffect(() => {
    if (!exam || exam.status !== 'IN_PROGRESS' || submitting) return;

    const timer = window.setInterval(() => {
      setRemainingSeconds((seconds) => Math.max(0, seconds - 1));
    }, 1000);

    return () => window.clearInterval(timer);
  }, [exam, submitting]);

  useEffect(() => {
    if (
      remainingSeconds === 0 &&
      exam?.status === 'IN_PROGRESS' &&
      !submitStartedRef.current &&
      !loading
    ) {
      void handleSubmit();
    }
    // handleSubmit is intentionally invoked only when the timer expires.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [remainingSeconds, exam, loading]);

  // Autosave every 15 seconds while an exam is active.
  useEffect(() => {
    if (!exam || exam.status !== 'IN_PROGRESS' || !token) return;

    const autosaveTimer = window.setInterval(() => {
      if (Date.now() - lastSaveTimeRef.current >= 15000) {
        void saveAnswers();
      }
    }, 15000);

    return () => window.clearInterval(autosaveTimer);
  }, [exam, token, saveAnswers]);

  function handleAnswerChange(questionId: number, value: string) {
    setAnswers((previous) => {
      const updated = {
        ...previous,
        [String(questionId)]: value,
      };

      answersRef.current = updated;
      return updated;
    });
    setNotice('');
  }

  async function enterFullscreen() {
    try {
      if (!document.fullscreenElement) {
        await document.documentElement.requestFullscreen();
      }
    } catch {
      setNotice(
        'Fullscreen could not be enabled. Your exam can continue, but the exit may be recorded.',
      );
    }
  }

  async function handleSubmit() {
    if (!token || !exam || submitStartedRef.current) return;

    const confirmed = window.confirm(
      'Are you sure you want to submit your exam?',
    );

    if (!confirmed && remainingSeconds > 0) return;

    submitStartedRef.current = true;
    setSubmitting(true);
    setError('');

    try {
      const answerList = Object.entries(answersRef.current).map(
        ([questionId, selectedOption]) => ({
          questionId: Number(questionId),
          selectedOption,
        }),
      );

      await autosaveExamAnswers(token, exam.examId, answerList);
      await submitExam(token, exam.examId);
      navigate('/student/dashboard', { replace: true });
    } catch {
      submitStartedRef.current = false;
      setSubmitting(false);
      setError(
        'Submission failed. Please check your connection and try again.',
      );
    }
  }

  function formatTime(seconds: number) {
    const minutes = Math.floor(seconds / 60);
    const remaining = seconds % 60;

    return `${String(minutes).padStart(2, '0')}:${String(remaining).padStart(2, '0')}`;
  }

  if (loading) {
    return (
      <main className="student-exam-page">
        <p>Loading exam...</p>
      </main>
    );
  }

  if (error && !exam) {
    return (
      <main className="student-exam-page">
        <h2>Unable to open exam</h2>
        <p role="alert">{error}</p>
        <Link to="/student/dashboard">Return to dashboard</Link>
      </main>
    );
  }

  if (!exam) return null;

  return (
    <main className="student-exam-page">
      <header className="student-exam-header">
        <div>
          <h1>{exam.title}</h1>
          <p>{exam.description}</p>
        </div>

        <div className="student-exam-timer" aria-live="polite">
          <span>Time remaining</span>
          <strong>{formatTime(remainingSeconds)}</strong>
        </div>
      </header>

      {error && <p className="exam-error" role="alert">{error}</p>}
      {notice && <p className="exam-notice" role="status">{notice}</p>}

      <section className="proctoring-status" aria-label="Proctoring status">
        <strong>Proctoring incidents recorded in this session</strong>
        <p>Tab switches: {incidentCounts.TAB_SWITCH}</p>
        <p>Fullscreen exits: {incidentCounts.FULLSCREEN_EXIT}</p>
        <button type="button" onClick={enterFullscreen}>
          Enter fullscreen
        </button>
      </section>

      {exam.questions.map((question, index) => (
        <section className="candidate-question" key={question.id}>
          <h2>
            Question {index + 1}
            <span> ({question.marks} marks)</span>
          </h2>

          <p>{question.prompt}</p>

          {question.options.length > 0 ? (
            <div className="candidate-options">
              {question.options.map((option) => (
                <label key={option}>
                  <input
                    type="radio"
                    name={`question-${question.id}`}
                    value={option}
                    checked={answers[String(question.id)] === option}
                    onChange={() =>
                      handleAnswerChange(question.id, option)
                    }
                    disabled={submitting || exam.status !== 'IN_PROGRESS'}
                  />
                  {option}
                </label>
              ))}
            </div>
          ) : (
            <textarea
              value={answers[String(question.id)] ?? ''}
              onChange={(event) =>
                handleAnswerChange(question.id, event.target.value)
              }
              placeholder="Enter your answer"
              disabled={submitting || exam.status !== 'IN_PROGRESS'}
            />
          )}
        </section>
      ))}

      <footer className="student-exam-actions">
        <button
          type="button"
          onClick={() => void saveAnswers()}
          disabled={submitting || exam.status !== 'IN_PROGRESS'}
        >
          Save answers
        </button>

        <button
          type="button"
          onClick={() => void handleSubmit()}
          disabled={submitting || exam.status !== 'IN_PROGRESS'}
        >
          {submitting ? 'Submitting...' : 'Submit exam'}
        </button>
      </footer>
    </main>
  );
}
