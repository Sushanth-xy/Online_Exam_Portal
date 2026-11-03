
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1/exams';

export interface CandidateQuestion {
  id: number;
  prompt: string;
  questionType: 'MCQ' | 'TRUE_FALSE' | 'SHORT_ANSWER';
  options: string[];
  marks: number;
}

export interface ExamAttemptView {
  examId: number;
  attemptId: number;
  title: string;
  description: string;
  durationMinutes: number;
  startedAt: string;
  remainingSeconds: number;
  status: 'IN_PROGRESS' | 'COMPLETED' | 'TIMED_OUT';
  questions: CandidateQuestion[];
  savedAnswers: Record<string, string>;
}

export interface AutosaveAnswer {
  questionId: number;
  selectedOption: string;
}

function authConfig(token: string) {
  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };
}

export async function startExam(
  examId: number,
  token: string,
): Promise<ExamAttemptView> {
  const response = await axios.post<ExamAttemptView>(
    `${API_BASE_URL}/${examId}/start`,
    {},
    authConfig(token),
  );

  return response.data;
}

export async function getActiveAttempt(
  examId: number,
  token: string,
): Promise<ExamAttemptView> {
  const response = await axios.get<ExamAttemptView>(
    `${API_BASE_URL}/${examId}/attempt`,
    authConfig(token),
  );

  return response.data;
}

export async function autosaveAnswers(
  examId: number,
  token: string,
  answers: AutosaveAnswer[],
): Promise<void> {
  await axios.post(
    `${API_BASE_URL}/${examId}/autosave`,
    { answers },
    authConfig(token),
  );
}

export async function submitExam(
  examId: number,
  token: string,
): Promise<void> {
  await axios.post(
    `${API_BASE_URL}/${examId}/submit`,
    {},
    authConfig(token),
  );
}
