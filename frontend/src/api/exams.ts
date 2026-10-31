import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1/exams';

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

function getHeaders(token: string) {
  return {
    headers: {
      Authorization: `Bearer ${token}`,
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
