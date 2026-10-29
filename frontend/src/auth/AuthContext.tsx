
import {
  createContext,
  useContext,
  useState,
  type ReactNode,
} from 'react';

export type UserRole = 'STUDENT' | 'INSTRUCTOR' | 'ADMIN';

export interface AuthUser {
  userId: number;
  name: string;
  email: string;
  role: UserRole;
}

export interface AuthResponse {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: UserRole;
  tokenType: string;
}

interface AuthContextType {
  user: AuthUser | null;
  token: string | null;
  login: (response: AuthResponse) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const STORAGE_KEY = 'oep_auth';

function getStoredAuth(): {
  user: AuthUser;
  token: string;
} | null {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);

    if (!stored) return null;

    const parsed = JSON.parse(stored);

    if (
      !parsed.user ||
      typeof parsed.token !== 'string' ||
      !parsed.token ||
      !['STUDENT', 'INSTRUCTOR', 'ADMIN'].includes(parsed.user.role)
    ) {
      localStorage.removeItem(STORAGE_KEY);
      return null;
    }

    return parsed;
  } catch {
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [initialAuth] = useState(getStoredAuth);
  const [user, setUser] = useState<AuthUser | null>(
    initialAuth?.user ?? null,
  );
  const [token, setToken] = useState<string | null>(
    initialAuth?.token ?? null,
  );

  function login(response: AuthResponse) {
    const nextUser: AuthUser = {
      userId: response.userId,
      name: response.name,
      email: response.email,
      role: response.role,
    };

    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ user: nextUser, token: response.token }),
    );

    setUser(nextUser);
    setToken(response.token);
  }

  function logout() {
    localStorage.removeItem(STORAGE_KEY);
    setUser(null);
    setToken(null);
  }

  return (
    <AuthContext.Provider value={{ user, token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }

  return context;
}
