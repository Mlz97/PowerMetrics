// Mirrors AnalysisSessionResponse from the Spring API
export type ExerciseType = 'SQUAT' | 'VERTICAL_JUMP';

export type SessionState = 'WAITING' | 'PROCESSING' | 'FINISHED' | 'ERROR';

export interface RepDetail {
  repNumber: number;
  valid: boolean;
  minHipAngle: number | null;
  minKneeAngle: number | null;
  concentricVelocity: number | null;
}

export interface JumpDetail {
  jumpNumber: number;
  flightTime: number | null;
  heightFlightCm: number | null;
  takeoffVelocity: number | null;
  minKneeAngle: number | null;
  countermovementDepthCm: number | null;
  heightDisplacementCm: number | null;
  peakPowerW: number | null;
}

export interface AnalysisSession {
  id: number;
  exerciseType: ExerciseType | null;
  state: SessionState;
  /** ISO local date-time without zone, e.g. "2026-10-20T10:15:30.123". */
  createdAt: string | null;
  videoFps?: number | null;
  lowFpsWarning?: boolean | null;
  totalReps?: number | null;
  validReps?: number | null;
  avgConcentricVelocity?: number | null;
  estimatedRM?: number | null;
  weightKg?: number | null;
  reps?: RepDetail[] | null;
  bodyWeightKg?: number | null;
  heightCm?: number | null;
  totalJumps?: number | null;
  bestJumpHeightCm?: number | null;
  avgJumpHeightCm?: number | null;
  peakPowerW?: number | null;
  jumps?: JumpDetail[] | null;
  fatigueIndex?: number | null;
}

export const isJump = (session: AnalysisSession): boolean => session.exerciseType === 'VERTICAL_JUMP';

export const liftOf = (session: AnalysisSession): ExerciseType => session.exerciseType ?? 'SQUAT';
