
import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import {
  autosaveAnswers,
  startExam,
  submitExam,
  type ExamAttemptView,
} from '../api/examAttempt';
import './ExamTaking.css';

type QuestionStatus = 'answered' | 'unanswered' | 'flagged';

export function ExamTaking() {
  const { examId: examIdParam } = useParams();
  const examId = Number(examIdParam);
  const { token } = useAuth();
  const navigate = useNavigate();

  const [attempt, setAttempt] = useState<ExamAttemptView | null>(null);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [flagged, setFlagged] = useState<number[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [remainingSeconds, setRemainingSeconds] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [autosaveMessage, setAutosaveMessage] = useState('Not saved yet');
  const [reviewOpen, setReviewOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const answersRef = useRef(answers);
  const finishedRef = useRef(false);

  useEffect(() => {
    answersRef.current = answers;
  }, [answers]);

  useEffect(() => {
    if (!token || !Number.isInteger(examId) || examId <= 0) {
      setError('Invalid exam or missing authentication. Please sign in again.');
      setLoading(false);
      return;
    }

    let cancelled = false;

    async function loadAttempt() {
        if(!token)return;
      try {
        const data = await startExam(examId, token);
        if (cancelled) return;

        if (data.status !== 'IN_PROGRESS') {
          setError('This exam attempt is no longer in progress.');
          return;
        }

        setAttempt(data);
        setAnswers(data.savedAnswers ?? {});
        answersRef.current = data.savedAnswers ?? {};
        setRemainingSeconds(Math.max(0, data.remainingSeconds));
      } catch {
        if (!cancelled) {
          setError('Unable to load the exam. Check that the exam is available and try again.');
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    void loadAttempt();

    return () => {
      cancelled = true;
    };
  }, [examId, token]);

  const saveAnswers = useCallback(async () => {
    if (!token || !attempt || finishedRef.current) return;

    try {
      const payload = Object.entries(answersRef.current).map(
        ([questionId, selectedOption]) => ({
          questionId: Number(questionId),
          selectedOption,
        }),
      );

      await autosaveAnswers(examId, token, payload);
      setAutosaveMessage(`Saved at ${new Date().toLocaleTimeString()}`);
    } catch {
      setAutosaveMessage('Autosave failed — please check your connection');
    }
  }, [attempt, examId, token]);

  const finishExam = useCallback(async () => {
    if (!token || finishedRef.current || !attempt) return;

    finishedRef.current = true;
    setSubmitting(true);
    setReviewOpen(false);

    try {
      const payload = Object.entries(answersRef.current).map(
        ([questionId, selectedOption]) => ({
          questionId: Number(questionId),
          selectedOption,
        }),
      );

      await autosaveAnswers(examId, token, payload);
      await submitExam(examId, token);
      navigate('/student/dashboard', {
        replace: true,
        state: { message: 'Exam submitted successfully.' },
      });
    } catch {
      finishedRef.current = false;
      setSubmitting(false);
      setError('Submission failed. Check your connection and try submitting again.');
    }
  }, [attempt, examId, navigate, token]);

  useEffect(() => {
    if (!attempt || loading || finishedRef.current) return;

    const interval = window.setInterval(() => {
      setRemainingSeconds((seconds) => Math.max(0, seconds - 1));
    }, 1000);

    return () => window.clearInterval(interval);
  }, [attempt, loading]);

  useEffect(() => {
    if (!attempt || loading || finishedRef.current) return;

    const interval = window.setInterval(() => {
      void saveAnswers();
    }, 30_000);

    return () => window.clearInterval(interval);
  }, [attempt, loading, saveAnswers]);

  useEffect(() => {
    if (attempt && !loading && remainingSeconds <= 0 && !finishedRef.current) {
      void finishExam();
    }
  }, [attempt, finishExam, loading, remainingSeconds]);

  if (loading) {
    return <main className="exam-page"><p>Loading exam…</p></main>;
  }

  if (error && !attempt) {
    return (
      <main className="exam-page">
        <h2>Unable to open exam</h2>
        <p role="alert">{error}</p>
        <Link to="/student/dashboard">Return to dashboard</Link>
      </main>
    );
  }

  if (!attempt) return null;

  const question = attempt.questions[currentIndex];

  function questionStatus(index: number): QuestionStatus {
    const id = String(attempt!.questions[index].id);

    if (flagged.includes(index)) return 'flagged';
    if ((answers[id] ?? '').trim()) return 'answered';
    return 'unanswered';
  }

  function updateAnswer(value: string) {
    if (!question) return;

    const id = String(question.id);
    setAnswers((previous) => {
      const updated = { ...previous, [id]: value };
      answersRef.current = updated;
      return updated;
    });
  }

  function toggleFlag() {
    setFlagged((previous) =>
      previous.includes(currentIndex)
        ? previous.filter((index) => index !== currentIndex)
        : [...previous, currentIndex],
    );
  }

  const answeredCount = attempt.questions.filter((item) =>
    (answers[String(item.id)] ?? '').trim(),
  ).length;

  const flaggedCount = flagged.length;
  const minutes = Math.floor(remainingSeconds / 60);
  const seconds = remainingSeconds % 60;

  return (
    <main className="exam-page">
      <header className="exam-header">
        <div>
          <p className="exam-eyebrow">STUDENT EXAM</p>
          <h1>{attempt.title}</h1>
          <p>{attempt.description}</p>
        </div>
        <div
          className={`exam-timer ${remainingSeconds <= 60 ? 'urgent' : ''}`}
          role="timer"
          aria-live="off"
        >
          <span>Time remaining</span>
          <strong>
            {String(minutes).padStart(2, '0')}:
            {String(seconds).padStart(2, '0')}
          </strong>
        </div>
      </header>

      {error && <p className="exam-error" role="alert">{error}</p>}

      <div className="exam-layout">
        <section className="exam-question-panel">
          <div className="question-meta">
            <span>Question {currentIndex + 1} of {attempt.questions.length}</span>
            <span>{question?.marks ?? 0} marks</span>
          </div>

          {question ? (
            <>
              <h2 className="question-prompt">{question.prompt}</h2>

              {question.questionType === 'SHORT_ANSWER' ? (
                <textarea
                  className="short-answer"
                  aria-label="Your answer"
                  placeholder="Type your answer here…"
                  value={answers[String(question.id)] ?? ''}
                  onChange={(event) => updateAnswer(event.target.value)}
                  disabled={submitting}
                />
              ) : (
                <div className="answer-options">
                  {(question.options ?? []).map((option, index) => (
                    <label className="answer-option" key={`${option}-${index}`}>
                      <input
                        type="radio"
                        name={`question-${question.id}`}
                        value={option}
                        checked={answers[String(question.id)] === option}
                        onChange={() => updateAnswer(option)}
                        disabled={submitting}
                      />
                      <span>{option}</span>
                    </label>
                  ))}
                </div>
              )}

              <div className="question-actions">
                <button
                  type="button"
                  className="secondary-button"
                  onClick={toggleFlag}
                  disabled={submitting}
                >
                  {flagged.includes(currentIndex) ? 'Unflag question' : 'Flag for review'}
                </button>

                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => updateAnswer('')}
                  disabled={submitting}
                >
                  Clear answer
                </button>
              </div>
            </>
          ) : (
            <p>This exam has no questions.</p>
          )}

          <footer className="question-navigation">
            <button
              type="button"
              className="secondary-button"
              onClick={() => setCurrentIndex((index) => Math.max(0, index - 1))}
              disabled={currentIndex === 0 || submitting}
            >
              Previous
            </button>

            <button
              type="button"
              className="primary-button"
              onClick={() =>
                setCurrentIndex((index) =>
                  Math.min(attempt.questions.length - 1, index + 1),
                )
              }
              disabled={
                currentIndex >= attempt.questions.length - 1 || submitting
              }
            >
              Next question
            </button>
          </footer>
        </section>

        <aside className="exam-sidebar">
          <h2>Question palette</h2>
          <p className="palette-summary">
            {answeredCount} answered · {attempt.questions.length - answeredCount} unanswered
          </p>

          <div className="question-palette">
            {attempt.questions.map((item, index) => (
              <button
                type="button"
                key={item.id}
                className={`palette-item ${questionStatus(index)} ${
                  index === currentIndex ? 'current' : ''
                }`}
                onClick={() => setCurrentIndex(index)}
                aria-label={`Question ${index + 1}: ${questionStatus(index)}`}
                aria-current={index === currentIndex ? 'step' : undefined}
                disabled={submitting}
              >
                {index + 1}
              </button>
            ))}
          </div>

          <div className="palette-legend">
            <span><i className="legend-dot answered" /> Answered</span>
            <span><i className="legend-dot unanswered" /> Unanswered</span>
            <span><i className="legend-dot flagged" /> Flagged ({flaggedCount})</span>
          </div>

          <p className="autosave-status" role="status">{autosaveMessage}</p>

          <button
            type="button"
            className="primary-button submit-button"
            onClick={() => setReviewOpen(true)}
            disabled={submitting}
          >
            Review and submit
          </button>
        </aside>
      </div>

      {reviewOpen && (
        <div className="modal-backdrop" role="presentation">
          <section
            className="review-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="review-title"
          >
            <h2 id="review-title">Review your exam</h2>
            <p>Check your progress before submitting. You cannot change answers after submission.</p>

            <div className="review-stats">
              <p>Answered: <strong>{answeredCount}</strong></p>
              <p>Unanswered: <strong>{attempt.questions.length - answeredCount}</strong></p>
              <p>Flagged: <strong>{flaggedCount}</strong></p>
            </div>

            {attempt.questions.length - answeredCount > 0 && (
              <p className="review-warning">
                You still have unanswered questions. You can return to them before submitting.
              </p>
            )}

            <div className="modal-actions">
              <button
                type="button"
                className="secondary-button"
                onClick={() => setReviewOpen(false)}
                disabled={submitting}
              >
                Continue exam
              </button>
              <button
                type="button"
                className="primary-button"
                onClick={() => void finishExam()}
                disabled={submitting}
              >
                {submitting ? 'Submitting…' : 'Confirm submission'}
              </button>
            </div>
          </section>
        </div>
      )}
    </main>
  );
}
