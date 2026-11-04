import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import {
  getActiveAttempt,
  reportProctoringIncident,
  startExam,
} from '../api/exams';
import { StudentExam } from './StudentExam';

vi.mock('../auth/AuthContext', () => ({
  useAuth: () => ({ token: 'student-token' }),
}));

vi.mock('../api/exams', () => ({
  autosaveExamAnswers: vi.fn(),
  getActiveAttempt: vi.fn(),
  reportProctoringIncident: vi.fn(),
  startExam: vi.fn(),
  submitExam: vi.fn(),
}));

const activeAttempt = {
  examId: 7,
  attemptId: 42,
  title: 'Biology Exam',
  description: 'Unit test exam',
  durationMinutes: 30,
  startedAt: '2026-10-28T09:00:00',
  remainingSeconds: 300,
  status: 'IN_PROGRESS' as const,
  questions: [],
  savedAnswers: {},
};

function renderStudentExam() {
  return render(
    <MemoryRouter initialEntries={['/student/exams/7']}>
      <Routes>
        <Route path="/student/exams/:examId" element={<StudentExam />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('StudentExam proctoring events', () => {
  beforeEach(() => {
    vi.mocked(getActiveAttempt).mockResolvedValue(activeAttempt);
    vi.mocked(startExam).mockResolvedValue(activeAttempt);
    vi.mocked(reportProctoringIncident).mockResolvedValue({
      id: 1,
      attemptId: 42,
      examTitle: 'Biology Exam',
      studentEmail: 'candidate@example.com',
      type: 'TAB_SWITCH',
      occurredAt: '2026-10-28T09:01:00',
    });
    Object.defineProperty(document, 'visibilityState', {
      configurable: true,
      value: 'visible',
    });
    Object.defineProperty(document, 'fullscreenElement', {
      configurable: true,
      value: null,
    });
  });

  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  it('reports a tab switch when the document becomes hidden', async () => {
    renderStudentExam();
    await screen.findByText('Tab switches: 0');

    Object.defineProperty(document, 'visibilityState', {
      configurable: true,
      value: 'hidden',
    });
    fireEvent(document, new Event('visibilitychange'));

    await waitFor(() => {
      expect(reportProctoringIncident).toHaveBeenCalledWith(
        'student-token',
        42,
        'TAB_SWITCH',
      );
    });
    expect(await screen.findByText('Tab switches: 1')).toBeTruthy();
  });

  it('reports a window blur as a tab-switch incident', async () => {
    renderStudentExam();
    await screen.findByText('Tab switches: 0');

    window.dispatchEvent(new Event('blur'));

    await waitFor(() => {
      expect(reportProctoringIncident).toHaveBeenCalledWith(
        'student-token',
        42,
        'TAB_SWITCH',
      );
    });
    expect(await screen.findByText('Tab switches: 1')).toBeTruthy();
  });

  it('coalesces blur and visibility events from one tab switch', async () => {
    renderStudentExam();
    await screen.findByText('Tab switches: 0');

    window.dispatchEvent(new Event('blur'));
    Object.defineProperty(document, 'visibilityState', {
      configurable: true,
      value: 'hidden',
    });
    fireEvent(document, new Event('visibilitychange'));

    await waitFor(() => {
      expect(reportProctoringIncident).toHaveBeenCalledTimes(1);
    });
    expect(await screen.findByText('Tab switches: 1')).toBeTruthy();
  });

  it('records fullscreen exits against the active attempt and warns the candidate', async () => {
    renderStudentExam();
    await screen.findByText('Fullscreen exits: 0');

    fireEvent(document, new Event('fullscreenchange'));

    await waitFor(() => {
      expect(reportProctoringIncident).toHaveBeenCalledWith(
        'student-token',
        42,
        'FULLSCREEN_EXIT',
      );
    });
    expect(
      await screen.findByText('Fullscreen exit detected and recorded.'),
    ).toBeTruthy();
    expect(await screen.findByText('Fullscreen exits: 1')).toBeTruthy();
  });
});
