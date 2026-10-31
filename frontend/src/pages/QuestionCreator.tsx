
import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import {
  createQuestion,
  type QuestionRequest,
  type QuestionType,
} from '../api/questions';

export function QuestionCreator() {
  const { token } = useAuth();
  

  const [questionType, setQuestionType] =
    useState<QuestionType>('MCQ');
  const [prompt, setPrompt] = useState('');
  const [topic, setTopic] = useState('General');
  const [difficulty, setDifficulty] =
    useState<'EASY' | 'MEDIUM' | 'HARD'>('MEDIUM');
  const [options, setOptions] = useState(['', '', '', '']);
  const [correctAnswer, setCorrectAnswer] = useState('');
  const [marks, setMarks] = useState(1);
  const [negativeMarks, setNegativeMarks] = useState(0);
  const [explanation, setExplanation] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  function updateOption(index: number, value: string) {
    setOptions((current) =>
      current.map((option, i) => (i === index ? value : option)),
    );
  }

  function handleTypeChange(type: QuestionType) {
    setQuestionType(type);
    setCorrectAnswer('');
    setOptions(['', '', '', '']);
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setSuccess('');

    if (!token) {
      setError('Your session has expired. Please log in again.');
      return;
    }

    let finalOptions: string[] = [];

    if (questionType === 'MCQ') {
      finalOptions = options.map((option) => option.trim());

      if (finalOptions.some((option) => !option)) {
        setError('Please fill in all four options.');
        return;
      }

      if (!finalOptions.includes(correctAnswer)) {
        setError('Please select a valid correct answer.');
        return;
      }
    } else if (questionType === 'TRUE_FALSE') {
      finalOptions = ['True', 'False'];

      if (!['True', 'False'].includes(correctAnswer)) {
        setError('Please select the correct answer.');
        return;
      }
    } else if (!correctAnswer.trim()) {
      setError('Please enter the expected answer.');
      return;
    }

    const question: QuestionRequest = {
      prompt: prompt.trim(),
      questionType,
      difficulty,
      topic: topic.trim(),
      options: finalOptions,
      correctAnswer: correctAnswer.trim(),
      marks,
      negativeMarks,
      explanation: explanation.trim(),
    };

    try {
      setLoading(true);
      await createQuestion(token, question);
      setSuccess('Question created successfully.');

      setPrompt('');
      setCorrectAnswer('');
      setOptions(['', '', '', '']);
      setExplanation('');
    } catch {
      setError('Could not create the question. Please check the form and try again.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main style={styles.page}>
      <div style={styles.container}>
        <Link to="/instructor/dashboard" style={styles.backLink}>
          ← Back to Dashboard
        </Link>

        <h1>Create Question</h1>
        <p style={styles.subtitle}>
          Add a question to your question bank.
        </p>

        <form onSubmit={handleSubmit} style={styles.form}>
          <label style={styles.label} htmlFor="questionType">
            Question Type
          </label>
          <select
            id="questionType"
            value={questionType}
            onChange={(event) =>
              handleTypeChange(event.target.value as QuestionType)
            }
            style={styles.input}
          >
            <option value="MCQ">Multiple Choice (MCQ)</option>
            <option value="TRUE_FALSE">True / False</option>
            <option value="SHORT_ANSWER">Short Answer</option>
          </select>

          <label style={styles.label} htmlFor="prompt">
            Question
          </label>
          <textarea
            id="prompt"
            value={prompt}
            onChange={(event) => setPrompt(event.target.value)}
            placeholder="Enter your question"
            required
            rows={4}
            style={styles.input}
          />

          {questionType === 'MCQ' && (
            <>
              <h3>Answer Options</h3>
              {options.map((option, index) => (
                <div key={index}>
                  <label style={styles.label} htmlFor={`option-${index}`}>
                    Option {index + 1}
                  </label>
                  <input
                    id={`option-${index}`}
                    value={option}
                    onChange={(event) =>
                      updateOption(index, event.target.value)
                    }
                    placeholder={`Enter option ${index + 1}`}
                    style={styles.input}
                    required
                  />
                </div>
              ))}
            </>
          )}

          <label style={styles.label} htmlFor="correctAnswer">
            {questionType === 'SHORT_ANSWER'
              ? 'Expected Answer'
              : 'Correct Answer'}
          </label>

          {questionType === 'MCQ' && (
            <select
              id="correctAnswer"
              value={correctAnswer}
              onChange={(event) => setCorrectAnswer(event.target.value)}
              style={styles.input}
              required
            >
              <option value="">Select the correct option</option>
              {options.map((option, index) => (
                <option key={index} value={option} disabled={!option.trim()}>
                  {option || `Option ${index + 1}`}
                </option>
              ))}
            </select>
          )}

          {questionType === 'TRUE_FALSE' && (
            <select
              id="correctAnswer"
              value={correctAnswer}
              onChange={(event) => setCorrectAnswer(event.target.value)}
              style={styles.input}
              required
            >
              <option value="">Select the correct answer</option>
              <option value="True">True</option>
              <option value="False">False</option>
            </select>
          )}

          {questionType === 'SHORT_ANSWER' && (
            <input
              id="correctAnswer"
              value={correctAnswer}
              onChange={(event) => setCorrectAnswer(event.target.value)}
              placeholder="Enter the expected answer"
              style={styles.input}
              required
            />
          )}

          <div style={styles.row}>
            <div style={styles.field}>
              <label style={styles.label} htmlFor="topic">
                Topic
              </label>
              <input
                id="topic"
                value={topic}
                onChange={(event) => setTopic(event.target.value)}
                required
                style={styles.input}
              />
            </div>

            <div style={styles.field}>
              <label style={styles.label} htmlFor="difficulty">
                Difficulty
              </label>
              <select
                id="difficulty"
                value={difficulty}
                onChange={(event) =>
                  setDifficulty(
                    event.target.value as 'EASY' | 'MEDIUM' | 'HARD',
                  )
                }
                style={styles.input}
              >
                <option value="EASY">Easy</option>
                <option value="MEDIUM">Medium</option>
                <option value="HARD">Hard</option>
              </select>
            </div>
          </div>

          <div style={styles.row}>
            <div style={styles.field}>
              <label style={styles.label} htmlFor="marks">
                Marks
              </label>
              <input
                id="marks"
                type="number"
                min="0.1"
                step="0.1"
                value={marks}
                onChange={(event) => setMarks(Number(event.target.value))}
                required
                style={styles.input}
              />
            </div>

            <div style={styles.field}>
              <label style={styles.label} htmlFor="negativeMarks">
                Negative Marks
              </label>
              <input
                id="negativeMarks"
                type="number"
                min="0"
                step="0.1"
                value={negativeMarks}
                onChange={(event) =>
                  setNegativeMarks(Number(event.target.value))
                }
                style={styles.input}
              />
            </div>
          </div>

          <label style={styles.label} htmlFor="explanation">
            Explanation (optional)
          </label>
          <textarea
            id="explanation"
            value={explanation}
            onChange={(event) => setExplanation(event.target.value)}
            rows={3}
            placeholder="Explain why this answer is correct"
            style={styles.input}
          />

          {error && <p style={styles.error}>{error}</p>}
          {success && <p style={styles.success}>{success}</p>}

          <button type="submit" disabled={loading} style={styles.button}>
            {loading ? 'Creating...' : 'Create Question'}
          </button>
        </form>
      </div>
    </main>
  );
}

const styles: Record<string, React.CSSProperties> = {
  page: {
    minHeight: '100vh',
    padding: '40px 20px',
    background: '#f3f6fc',
  },
  container: {
    maxWidth: '720px',
    margin: '0 auto',
    padding: '32px',
    background: '#fff',
    border: '1px solid #e6eaf2',
    borderRadius: '16px',
    boxShadow: '0 12px 35px rgb(31 41 55 / 6%)',
  },
  backLink: {
    display: 'inline-block',
    marginBottom: '20px',
    fontWeight: 600,
  },
  subtitle: {
    color: '#697386',
    marginBottom: '28px',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
  },
  label: {
    display: 'block',
    marginTop: '10px',
    marginBottom: '4px',
    fontSize: '14px',
    fontWeight: 650,
  },
  input: {
    width: '100%',
    padding: '11px 12px',
    border: '1px solid #d5dbe7',
    borderRadius: '8px',
    font: 'inherit',
    background: '#fff',
  },
  row: {
    display: 'flex',
    gap: '16px',
  },
  field: {
    flex: 1,
    minWidth: 0,
  },
  button: {
    marginTop: '18px',
    padding: '13px',
    color: '#fff',
    background: '#4f46e5',
    border: 0,
    borderRadius: '8px',
    fontWeight: 700,
    cursor: 'pointer',
  },
  error: {
    padding: '10px',
    color: '#b42318',
    background: '#fef3f2',
    borderRadius: '8px',
  },
  success: {
    padding: '10px',
    color: '#067647',
    background: '#ecfdf3',
    borderRadius: '8px',
  },
};
