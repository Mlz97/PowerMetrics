import type { AnalysisSession } from './types';

/** Builds an AnalysisSession with only the fields a test cares about. */
export const testSession = (overrides: Partial<AnalysisSession> = {}): AnalysisSession => ({
  id: 1,
  exerciseType: 'SQUAT',
  state: 'FINISHED',
  createdAt: '2026-10-20T10:00:00',
  ...overrides,
});
