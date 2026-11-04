
import axios from 'axios';
import type { UserRole } from '../auth/AuthContext';

const API_BASE_URL = 'http://localhost:8080/api/v1/admin';

export interface AdminUser {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  active: boolean;
  createdAt: string;
}

export interface CreateAdminUserRequest {
  name: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface AuditLog {
  id: number;
  timestamp: string;
  userEmail: string;
  role: string;
  action: string;
  httpMethod: string;
  endpoint: string;
  ipAddress: string;
  httpStatus: number;
  details: string;
  archived: boolean;
  retentionUntil: string;
}


export interface ProctoringIncident {
  id: number;
  attemptId: number;
  examTitle: string;
  studentEmail: string;
  type: 'TAB_SWITCH' | 'FULLSCREEN_EXIT';
  occurredAt: string;
}


function adminApi(token: string) {
  return axios.create({
    baseURL: API_BASE_URL,
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  });
}

export async function getAdminUsers(token: string) {
  const response = await adminApi(token).get<AdminUser[]>('/users');
  return response.data;
}

export async function createAdminUser(
  token: string,
  user: CreateAdminUserRequest,
) {
  const response = await adminApi(token).post<AdminUser>('/users', user);
  return response.data;
}

export async function updateAdminUserRole(
  token: string,
  userId: number,
  role: UserRole,
) {
  const response = await adminApi(token).patch<AdminUser>(
    `/users/${userId}/role`,
    { role },
  );
  return response.data;
}

export async function updateAdminUserStatus(
  token: string,
  userId: number,
  active: boolean,
) {
  const response = await adminApi(token).patch<AdminUser>(
    `/users/${userId}/status`,
    { active },
  );
  return response.data;
}

export async function getAuditLogs(token: string, page = 0, size = 20) {
  const response = await adminApi(token).get('/audit-logs', {
    params: { page, size },
  });
  return response.data;
}


export async function getProctoringIncidents(
  token: string
): Promise<ProctoringIncident[]> {
  const response = await adminApi(token).get<ProctoringIncident[]>(
    '/proctoring/incidents'
  );
  return response.data;
}
