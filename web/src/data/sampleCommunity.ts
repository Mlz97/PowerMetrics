import type { CommunityEntry, CommunityMetric } from '../domain/community';
import type { ExerciseType } from '../domain/types';

/** Community leaderboards. The backend has no users or rankings yet, so only the sample board exists. */
export interface CommunityRepository {
  /** True while the boards show sample data instead of real athletes. */
  isSample: boolean;
  board(lift: ExerciseType): CommunityEntry[];
  you(lift: ExerciseType, bodyWeightKg: number): CommunityEntry;
  yourRank(metric: CommunityMetric): number;
  memberCount(lift: ExerciseType): number;
  context(lift: ExerciseType): string;
}

const SQUAT: CommunityEntry[] = [
  { name: 'Maya Rios', bodyWeightKg: 82.4, loadKg: 247.5, meanVelocity: 0.29, powerW: 2480 },
  { name: 'Jonas Keller', bodyWeightKg: 83.0, loadKg: 242.5, meanVelocity: 0.27, powerW: 2390 },
  { name: 'Tomás Iglesias', bodyWeightKg: 81.2, loadKg: 240.0, meanVelocity: 0.31, powerW: 2610 },
  { name: 'Priya Nair', bodyWeightKg: 80.1, loadKg: 235.0, meanVelocity: 0.33, powerW: 2550 },
  { name: 'Kenji Mori', bodyWeightKg: 82.9, loadKg: 232.5, meanVelocity: 0.28, powerW: 2300 },
  { name: 'Lena Fischer', bodyWeightKg: 77.5, loadKg: 228.0, meanVelocity: 0.35, powerW: 2700 },
];

const JUMP: CommunityEntry[] = [
  { name: 'Sam Okafor', bodyWeightKg: 82.0, jumpHeightCm: 68.4 },
  { name: 'Dario Conti', bodyWeightKg: 83.0, jumpHeightCm: 64.1 },
  { name: 'Lena Fischer', bodyWeightKg: 76.0, jumpHeightCm: 61.7 },
  { name: 'Priya Nair', bodyWeightKg: 80.5, jumpHeightCm: 59.2 },
  { name: 'Tomás Iglesias', bodyWeightKg: 81.4, jumpHeightCm: 57.8 },
  { name: 'Kenji Mori', bodyWeightKg: 83.0, jumpHeightCm: 55.3 },
];

const RANKS: Record<CommunityMetric, number> = {
  LOAD: 214, RELATIVE: 198, MEAN_VELOCITY: 121, POWER: 176,
  HEIGHT: 342, JUMP_POWER: 288, POWER_PER_KG: 301, FLIGHT_TIME: 342,
};

/** Sample boards from the PowerMetrics design; replace with a backend-backed implementation. */
export const sampleCommunity: CommunityRepository = {
  isSample: true,
  board: (lift) => (lift === 'VERTICAL_JUMP' ? JUMP : SQUAT),
  you: (lift, bodyWeightKg) =>
    lift === 'VERTICAL_JUMP'
      ? { name: 'You', bodyWeightKg, jumpHeightCm: 41.2 }
      : { name: 'You', bodyWeightKg, loadKg: 150, meanVelocity: 0.3, powerW: 1840 },
  yourRank: (metric) => RANKS[metric],
  memberCount: () => 3902,
  context: (lift) =>
    lift === 'VERTICAL_JUMP' ? 'All bodyweights · this month · video-verified' : '83 kg class · this month · video-verified',
};
