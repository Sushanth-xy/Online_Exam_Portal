
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1/exams';
const PROCTORING_URL = 'http://localhost:8080/api/v1/proctoring';

export interface ExamRequest {
  title: string;
  description: string;
  durationMinutes: number;
  startTime: string;
  endTime: string;
  passPercentage: number;
  negativeMarkingFactor: number;
  shuffleQuestions: boolean;
  questionIds: number[];
}

export interface Exam {
  id: number;
  title: string;
  description: string;
  durationMinutes: number;
  startTime: string;
  endTime: string;
  passPercentage: number;
  negativeMarkingFactor: number;
  shuffleQuestions: boolean;
  questionIds?: number[];
}

export interface CandidateQuestion {
  id: number;
  prompt: string;
  questionType: string;
  options: string[];
  marks: number;
}

export interface CandidateExam {
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

export interface AutosaveAnswerItem {
  questionId: number;
  selectedOption: string;
}

export interface ProctoringIncidentResponse {
  id: number;
  attemptId: number;
  examTitle: string;
  studentEmail: string;
  type: 'TAB_SWITCH' | 'FULLSCREEN_EXIT';
  occurredAt: string;
}

function getHeaders(token: string) {
  return {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  };
}

export async function createExam(
  token: string,
  exam: ExamRequest,
): Promise<Exam> {
  const response = await axios.post<Exam>(
    API_BASE_URL,
    exam,
    getHeaders(token),
  );
  return response.data;
}

export async function getExams(token: string): Promise<Exam[]> {
  const response = await axios.get<Exam[]>(
    API_BASE_URL,
    getHeaders(token),
  );
  return response.data;
}

export async function startExam(
  token: string,
  examId: number,
): Promise<CandidateExam> {
  const response = await axios.post<CandidateExam>(
    `${API_BASE_URL}/${examId}/start`,
    {},
    getHeaders(token),
  );
  return response.data;
}

export async function getActiveAttempt(
  token: string,
  examId: number,
): Promise<CandidateExam> {
  const response = await axios.get<CandidateExam>(
    `${API_BASE_URL}/${examId}/attempt`,
    getHeaders(token),
  );
  return response.data;
}

export async function autosaveExamAnswers(
  token: string,
  examId: number,
  answers: AutosaveAnswerItem[],
) {
  const response = await axios.post(
    `${API_BASE_URL}/${examId}/autosave`,
    { answers },
    getHeaders(token),
  );
  return response.data;
}

export async function submitExam(
  token: string,
  examId: number,
) {
  const response = await axios.post(
    `${API_BASE_URL}/${examId}/submit`,
    {},
    getHeaders(token),
  );
  return response.data;
}

export async function reportProctoringIncident(
  token: string,
  attemptId: number,
  type: 'TAB_SWITCH' | 'FULLSCREEN_EXIT',
): Promise<ProctoringIncidentResponse> {
  const response = await axios.post<ProctoringIncidentResponse>(
    `${PROCTORING_URL}/attempts/${attemptId}/incidents`,
    { type },
    getHeaders(token),
  );
  return response.data;
}
