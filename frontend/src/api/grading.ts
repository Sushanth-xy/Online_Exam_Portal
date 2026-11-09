import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1/grading';

export interface CandidateAnswerDTO {
  questionId: number;
  questionPrompt: string;
  questionType: string;
  selectedOption: string;
  maxMarks: number;
  awardedMarks: number | null;
  feedback: string | null;
  autoGraded: boolean;
}

export interface GradingSubmissionDTO {
  attemptId: number;
  examId: number;
  examTitle: string;
  studentEmail: string;
  status: string;
  totalScore: number;
  allSubjectiveEvaluated: boolean;
  submittedAt: string;
  answers: CandidateAnswerDTO[];
}

export interface AnswerGradeItem {
  questionId: number;
  awardedMarks: number;
  feedback?: string;
}

export async function getSubmissions(token: string): Promise<GradingSubmissionDTO[]> {
  const response = await axios.get<GradingSubmissionDTO[]>(`${API_BASE_URL}/submissions`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return response.data;
}

export async function getSubmissionDetail(
  token: string,
  submissionId: number,
): Promise<GradingSubmissionDTO> {
  const response = await axios.get<GradingSubmissionDTO>(
    `${API_BASE_URL}/submissions/${submissionId}`,
    {
      headers: { Authorization: `Bearer ${token}` },
    },
  );
  return response.data;
}

export async function gradeSubmission(
  token: string,
  submissionId: number,
  grades: AnswerGradeItem[],
): Promise<GradingSubmissionDTO> {
  const response = await axios.post<GradingSubmissionDTO>(
    `${API_BASE_URL}/submissions/${submissionId}/grade`,
    { grades },
    {
      headers: { Authorization: `Bearer ${token}` },
    },
  );
  return response.data;
}
