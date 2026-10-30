import { parseCreatedAt } from './homeStats';
import type { UserProfile } from './profile';
import { isJump, type AnalysisSession } from './types';
import { formatWeight } from './units';

/** "150 kg × 3" for a squat, "41.2 cm best" for a jump. */
export function sessionHeadline(session: AnalysisSession, profile: UserProfile): string {
  if (isJump(session)) {
    return session.bestJumpHeightCm != null ? `${session.bestJumpHeightCm.toFixed(1)} cm best` : `${session.totalJumps ?? 0} jumps`;
  }
  const reps = session.validReps ?? session.totalReps ?? 0;
  return session.weightKg != null ? `${formatWeight(session.weightKg, profile.unit)} × ${reps}` : `${reps} reps`;
}

const date = (session: AnalysisSession) => parseCreatedAt(session.createdAt);

/** "Oct 20" from the session timestamp. */
export const shortDate = (session: AnalysisSession): string =>
  date(session)?.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) ?? '—';

export const dayOfMonth = (session: AnalysisSession): string => date(session)?.getDate().toString() ?? '—';

export const monthShort = (session: AnalysisSession): string =>
  date(session)?.toLocaleDateString('en-US', { month: 'short' }).toUpperCase() ?? '';
