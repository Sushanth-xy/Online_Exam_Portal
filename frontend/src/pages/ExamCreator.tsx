
import { useEffect, useState, type FormEvent } from 'react';
import axios from 'axios';
import { useAuth } from '../auth/AuthContext';
import { getQuestions, type Question } from '../api/questions';
import { createExam } from '../api/exams';

export function ExamCreator() {
  const { token } = useAuth();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [durationMinutes, setDurationMinutes] = useState(60);
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [passPercentage, setPassPercentage] = useState(40);
  const [negativeMarkingFactor, setNegativeMarkingFactor] = useState(0);
  const [shuffleQuestions, setShuffleQuestions] = useState(false);

  const [questions, setQuestions] = useState<Question[]>([]);
  const [selectedQuestionIds, setSelectedQuestionIds] = useState<number[]>([]);

  const [loadingQuestions, setLoadingQuestions] = useState(true);
  const [creating, setCreating] = useState(false);
  const [created, setCreated] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Preview state
  const [previewMode, setPreviewMode] = useState(false);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [previewAnswers, setPreviewAnswers] = useState<Record<number, string>>({});

  useEffect(() => {
    async function loadQuestions() {
      if (!token) {
        setLoadingQuestions(false);
        return;
      }

      try {
        const data = await getQuestions(token);
        setQuestions(data);
      } catch {
        setError('Unable to load questions. Please try again.');
      } finally {
        setLoadingQuestions(false);
      }
    }

    loadQuestions();
  }, [token]);

  function toggleQuestion(id: number) {
    setSelectedQuestionIds((previous) =>
      previous.includes(id)
        ? previous.filter((questionId) => questionId !== id)
        : [...previous, id],
    );
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    // Prevent duplicate submissions.
    if (creating || created) return;

    setError('');
    setSuccess('');

    if (!token) {
      setError('Please log in again.');
      return;
    }

    if (!title.trim()) {
      setError('Enter an exam title.');
      return;
    }

    if (selectedQuestionIds.length === 0) {
      setError('Select at least one question.');
      return;
    }

    if (!startTime || !endTime || new Date(startTime) >= new Date(endTime)) {
      setError('End time must be after start time.');
      return;
    }

    setCreating(true);

    try {
      const exam = await createExam(token, {
        title: title.trim(),
        description: description.trim(),
        durationMinutes,
        startTime,
        endTime,
        passPercentage,
        negativeMarkingFactor,
        shuffleQuestions,
        questionIds: selectedQuestionIds,
      });

      setSuccess(`Exam "${exam.title}" created successfully!`);
      setCreated(true);
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) {
        setError(
          err.response?.data?.message ||
            'Failed to create exam. Please check your details.',
        );
      } else {
        setError('Something went wrong. Please try again.');
      }
    } finally {
      setCreating(false);
    }
  }

  const selectedQuestions = questions.filter((question) =>
    selectedQuestionIds.includes(question.id),
  );

  const selectedMarks = selectedQuestions.reduce(
    (total, question) => total + question.marks,
    0,
  );

  function startPreview() {
    if (!title.trim()) {
      setError('Enter an exam title before previewing.');
      return;
    }

    if (selectedQuestions.length === 0) {
      setError('Select at least one question before previewing.');
      return;
    }

    setError('');
    setPreviewAnswers({});
    setCurrentQuestionIndex(0);
    setPreviewMode(true);
  }

  function closePreview() {
    setPreviewMode(false);
    setCurrentQuestionIndex(0);
  }

  function savePreviewAnswer(questionId: number, answer: string) {
    setPreviewAnswers((previous) => ({
      ...previous,
      [questionId]: answer,
    }));
  }

  if (previewMode) {
    const question = selectedQuestions[currentQuestionIndex];

    return (
      <main className="dashboard-page">
        <section className="dashboard-container">
          <div className="preview-header">
            <div>
              <p>STUDENT VIEW · PREVIEW ONLY</p>
              <h1>{title}</h1>
              <p>{description || 'No exam description provided.'}</p>
            </div>

            <button type="button" onClick={closePreview}>
              Exit Preview
            </button>
          </div>

          <div className="preview-info">
            <span>Duration: {durationMinutes} minutes</span>
            <span>Questions: {selectedQuestions.length}</span>
            <span>Total marks: {selectedMarks}</span>
          </div>

          <div className="preview-question">
            <div className="preview-question-header">
              <h2>Question {currentQuestionIndex + 1}</h2>
              <span>{question.marks} marks</span>
            </div>

            <p>{question.prompt}</p>

            {question.questionType === 'MCQ' && (
              <div className="preview-options">
                {question.options.map((option, index) => (
                  <label key={index} className="preview-option">
                    <input
                      type="radio"
                      name={`question-${question.id}`}
                      value={option}
                      checked={previewAnswers[question.id] === option}
                      onChange={() =>
                        savePreviewAnswer(question.id, option)
                      }
                    />
                    {option}
                  </label>
                ))}
              </div>
            )}

            {question.questionType === 'TRUE_FALSE' && (
              <div className="preview-options">
                {['True', 'False'].map((option) => (
                  <label key={option} className="preview-option">
                    <input
                      type="radio"
                      name={`question-${question.id}`}
                      value={option}
                      checked={previewAnswers[question.id] === option}
                      onChange={() =>
                        savePreviewAnswer(question.id, option)
                      }
                    />
                    {option}
                  </label>
                ))}
              </div>
            )}

            {question.questionType === 'SHORT_ANSWER' && (
              <textarea
                rows={4}
                placeholder="Type your answer here..."
                value={previewAnswers[question.id] || ''}
                onChange={(event) =>
                  savePreviewAnswer(question.id, event.target.value)
                }
              />
            )}
          </div>

          <div className="preview-navigation">
            <button
              type="button"
              disabled={currentQuestionIndex === 0}
              onClick={() =>
                setCurrentQuestionIndex((previous) => previous - 1)
              }
            >
              Previous
            </button>

            <span>
              {currentQuestionIndex + 1} / {selectedQuestions.length}
            </span>

            <button
              type="button"
              disabled={currentQuestionIndex === selectedQuestions.length - 1}
              onClick={() =>
                setCurrentQuestionIndex((previous) => previous + 1)
              }
            >
              Next
            </button>
          </div>

          <p className="preview-notice">
            This is a preview. Your answers are temporary and will not be
            submitted or saved.
          </p>
        </section>
      </main>
    );
  }

  return (
    <main className="dashboard-page">
      <section className="dashboard-container">
        <h1>Create Exam</h1>
        <p>Configure your exam and select questions from the question bank.</p>

        <form onSubmit={handleSubmit} className="exam-form">
          <h2>Exam Details</h2>

          <label htmlFor="exam-title">Exam title</label>
          <input
            id="exam-title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Enter exam title"
            required
          />

          <label htmlFor="exam-description">Description</label>
          <textarea
            id="exam-description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Enter exam instructions or description"
            rows={3}
          />

          <label htmlFor="duration">Duration (minutes)</label>
          <input
            id="duration"
            type="number"
            min={1}
            value={durationMinutes}
            onChange={(e) => setDurationMinutes(Number(e.target.value))}
            required
          />

          <label htmlFor="start-time">Start time</label>
          <input
            id="start-time"
            type="datetime-local"
            value={startTime}
            onChange={(e) => setStartTime(e.target.value)}
            required
          />

          <label htmlFor="end-time">End time</label>
          <input
            id="end-time"
            type="datetime-local"
            value={endTime}
            onChange={(e) => setEndTime(e.target.value)}
            required
          />

          <label htmlFor="pass-percentage">Pass percentage</label>
          <input
            id="pass-percentage"
            type="number"
            min={0}
            max={100}
            value={passPercentage}
            onChange={(e) => setPassPercentage(Number(e.target.value))}
            required
          />

          <label htmlFor="negative-marking">
            Negative marking factor
          </label>
          <input
            id="negative-marking"
            type="number"
            min={0}
            step="0.1"
            value={negativeMarkingFactor}
            onChange={(e) =>
              setNegativeMarkingFactor(Number(e.target.value))
            }
            required
          />

          <label className="exam-checkbox">
            <input
              type="checkbox"
              checked={shuffleQuestions}
              onChange={(e) => setShuffleQuestions(e.target.checked)}
            />
            Shuffle questions
          </label>

          <hr />

          <h2>Select Questions</h2>

          {loadingQuestions ? (
            <p>Loading questions...</p>
          ) : questions.length === 0 ? (
            <p>No questions available. Create questions first.</p>
          ) : (
            <div className="question-selection">
              {questions.map((question) => (
                <label key={question.id} className="question-option">
                  <input
                    type="checkbox"
                    checked={selectedQuestionIds.includes(question.id)}
                    onChange={() => toggleQuestion(question.id)}
                  />

                  <span>
                    <strong>{question.prompt}</strong>
                    <br />
                    {question.questionType} · {question.difficulty} ·{' '}
                    {question.marks} marks
                  </span>
                </label>
              ))}
            </div>
          )}

          <p>
            **Selected:** {selectedQuestionIds.length} questions ·{' '}
            **Total marks:** {selectedMarks}
          </p>

          {error && <p className="auth-error">{error}</p>}
          {success && <p role="status">{success}</p>}

          <div className="exam-actions">
            <button
              type="button"
              onClick={startPreview}
              disabled={loadingQuestions}
            >
              Preview as Student
            </button>

            <button
              type="submit"
              disabled={creating || loadingQuestions || created}
            >
              {creating
                ? 'Creating exam...'
                : created
                  ? 'Exam Created'
                  : 'Create Exam'}
            </button>
          </div>
        </form>
      </section>
    </main>
  );
}
