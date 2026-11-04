import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1/instructor';

export interface ProctoringIncident {
  id: number;
  attemptId: number;
  examTitle: string;
  studentEmail: string;
  type: 'TAB_SWITCH' | 'FULLSCREEN_EXIT';
  occurredAt: string;
}

export async function getInstructorProctoringIncidents(
  token: string,
): Promise<ProctoringIncident[]> {
  const response = await axios.get<ProctoringIncident[]>(
    `${API_BASE_URL}/proctoring/incidents`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    },
  );
  return response.data;
}
