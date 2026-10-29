
import axios from 'axios';
import type { AuthResponse } from '../auth/AuthContext';

const API_BASE_URL = 'http://localhost:8080/api/v1/auth';

export async function loginRequest(email: string, password: string) {
  const response = await axios.post<AuthResponse>(
    `${API_BASE_URL}/login`,
    { email, password },
  );

  return response.data;
}

export async function registerRequest(
  name: string,
  email: string,
  password: string,
) {
  const response = await axios.post<AuthResponse>(
    `${API_BASE_URL}/register`,
    { name, email, password },
  );

  return response.data;
}
