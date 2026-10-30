import { liftOf, type AnalysisSession, type ExerciseType } from './types';

const STATS_WINDOW_MS = 30 * 24 * 60 * 60 * 1000;
const CHART_POINTS = 8;

/** Parses the backend's ISO local date-time ("2026-10-20T10:15:30.123"); null when missing or malformed. */
export function parseCreatedAt(createdAt: string | null | undefined): Date | null {
  if (!createdAt || createdAt.length < 19) return null;
  const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})/.exec(createdAt);
  if (!match) return null;
  const [, y, mo, d, h, mi, s] = match.map(Number);
  const date = new Date(y, mo - 1, d, h, mi, s);
  return Number.isNaN(date.getTime()) ? null : date;
}

const finishedOf = (sessions: AnalysisSession[], lift: ExerciseType) =>
  sessions.filter((s) => s.state === 'FINISHED' && liftOf(s) === lift);

const maxOf = (values: (number | null | undefined)[]): number | null => {
  const present = values.filter((v): v is number => v != null);
  return present.length ? Math.max(...present) : null;
};

export interface SquatStats {
  best1RmKg: number | null;
  previousBest1RmKg: number | null;
  bestVelocity: number | null;
  previousBestVelocity: number | null;
  validReps: number;
}

export interface JumpStats {
  bestHeightCm: number | null;
  previousBestHeightCm: number | null;
  bestPeakPowerW: number | null;
  previousBestPeakPowerW: number | null;
}

export interface HomeStats {
  squat: SquatStats;
  jump: JumpStats;
}

/** Bests of the last 30 days for the Home panel, each with the best before that window to show progress. */
export function homeStats(sessions: AnalysisSession[], now: Date): HomeStats {
  const windowStart = now.getTime() - STATS_WINDOW_MS;
  const split = (list: AnalysisSession[]) => {
    const dated = list.flatMap((s) => {
      const date = parseCreatedAt(s.createdAt);
      return date ? [{ s, time: date.getTime() }] : [];
    });
    return [
      dated.filter((d) => d.time >= windowStart).map((d) => d.s),
      dated.filter((d) => d.time < windowStart).map((d) => d.s),
    ] as const;
  };

  const [squats, oldSquats] = split(finishedOf(sessions, 'SQUAT'));
  const [jumps, oldJumps] = split(finishedOf(sessions, 'VERTICAL_JUMP'));
  return {
    squat: {
      best1RmKg: maxOf(squats.map((s) => s.estimatedRM)),
      previousBest1RmKg: maxOf(oldSquats.map((s) => s.estimatedRM)),
      bestVelocity: maxOf(squats.map((s) => s.avgConcentricVelocity)),
      previousBestVelocity: maxOf(oldSquats.map((s) => s.avgConcentricVelocity)),
      validReps: squats.reduce((sum, s) => sum + (s.validReps ?? 0), 0),
    },
    jump: {
      bestHeightCm: maxOf(jumps.map((s) => s.bestJumpHeightCm)),
      previousBestHeightCm: maxOf(oldJumps.map((s) => s.bestJumpHeightCm)),
      bestPeakPowerW: maxOf(jumps.map((s) => s.peakPowerW)),
      previousBestPeakPowerW: maxOf(oldJumps.map((s) => s.peakPowerW)),
    },
  };
}

const timeOf = (s: AnalysisSession) => parseCreatedAt(s.createdAt)?.getTime() ?? Number.MIN_SAFE_INTEGER;

/** Most recent finished session of any lift, for the Home "Historic" card. */
export function lastFinishedSession(sessions: AnalysisSession[]): AnalysisSession | null {
  const finished = sessions.filter((s) => s.state === 'FINISHED');
  return finished.reduce<AnalysisSession | null>((best, s) => (best === null || timeOf(s) > timeOf(best) ? s : best), null);
}

/** Finished sessions of one lift, newest first. */
export const sessionsOf = (sessions: AnalysisSession[], lift: ExerciseType): AnalysisSession[] =>
  finishedOf(sessions, lift).sort((a, b) => timeOf(b) - timeOf(a));

/** Progress chart of the Historic tab: estimated 1RM (squat) or best height (jump) of the last 8 sessions, oldest first. */
export const historyChart = (sessions: AnalysisSession[], lift: ExerciseType): number[] =>
  sessionsOf(sessions, lift)
    .map((s) => (lift === 'VERTICAL_JUMP' ? s.bestJumpHeightCm : s.estimatedRM))
    .filter((v): v is number => v != null)
    .slice(0, CHART_POINTS)
    .reverse();
