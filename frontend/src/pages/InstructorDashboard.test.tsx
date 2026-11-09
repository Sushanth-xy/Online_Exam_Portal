import { cleanup, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { getInstructorProctoringIncidents } from '../api/proctoring';
import { InstructorDashboard } from './InstructorDashboard';

vi.mock('../auth/AuthContext', () => ({
  useAuth: () => ({ token: 'instructor-token' }),
}));

vi.mock('../api/proctoring', () => ({
  getInstructorProctoringIncidents: vi.fn(),
}));

vi.mock('../api/grading', () => ({
  getSubmissions: vi.fn().mockResolvedValue([]),
  gradeSubmission: vi.fn(),
}));

describe('InstructorDashboard proctoring monitoring', () => {
  beforeEach(() => {
    vi.mocked(getInstructorProctoringIncidents).mockResolvedValue([
      {
        id: 9,
        attemptId: 42,
        examTitle: 'Biology Exam',
        studentEmail: 'candidate@example.com',
        type: 'FULLSCREEN_EXIT',
        occurredAt: '2026-10-28T09:01:00',
      },
    ]);
  });

  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  it('shows timestamped candidate incidents returned for instructor monitoring', async () => {
    render(
      <MemoryRouter>
        <InstructorDashboard />
      </MemoryRouter>,
    );

    expect(await screen.findByText('candidate@example.com')).toBeTruthy();
    expect(screen.getByText('Biology Exam')).toBeTruthy();
    expect(screen.getByText('Fullscreen Exit')).toBeTruthy();
    expect(getInstructorProctoringIncidents).toHaveBeenCalledWith(
      'instructor-token',
    );
    expect(screen.getByRole('columnheader', { name: 'Timestamp' })).toBeTruthy();
  });
});
