import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1/questions';

export type QuestionType = 'MCQ' | 'TRUE_FALSE' | 'SHORT_ANSWER';

export interface Question {
  id: number;
  prompt: string;
  questionType: QuestionType;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  topic: string;
  options: string[];
  correctAnswer: string;
  marks: number;
  negativeMarks: number;
  explanation?: string;
  createdBy?: string;
}

export interface QuestionRequest {
  prompt: string;
  questionType: QuestionType;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  topic: string;
  options: string[];
  correctAnswer: string;
  marks: number;
  negativeMarks: number;
  explanation: string;
}

function getHeaders(token: string) {
  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };
}

export async function getQuestions(token: string): Promise<Question[]> {
  const response = await axios.get<Question[]>(
    API_BASE_URL,
    getHeaders(token),
  );

  return response.data;
}

export async function createQuestion(
  token: string,
  question: QuestionRequest,
): Promise<Question> {
  const response = await axios.post<Question>(
    API_BASE_URL,
    question,
    getHeaders(token),
  );

  return response.data;
}

export async function deleteQuestion(
  token: string,
  questionId: number,
): Promise<void> {
  await axios.delete(
    `${API_BASE_URL}/${questionId}`,
    getHeaders(token),
  );
}