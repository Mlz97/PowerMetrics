import { flightTimeMs } from './jumpMath';
import { isJump, type AnalysisSession } from './types';
import { formatInt, formatWeight, type WeightUnit } from './units';

/** How a rep/jump bar is highlighted in the result sheet. */
export type BarTone = 'top' | 'normal' | 'low' | 'invalid';

export interface ResultBar {
  label: string;
  fraction: number;
  tone: BarTone;
}

export interface ResultStat {
  label: string;
  value: string;
}

/** Everything the Analyze result sheet shows for a finished session. */
export interface ResultSummary {
  title: string;
  note: string;
  bars: ResultBar[];
  stats: ResultStat[];
}

// Velocity loss above this means the set went close to failure
const HIGH_VELOCITY_LOSS = 0.25;
// A rep slower than this share of the first one is drawn as slow
const SLOW_REP_SHARE = 0.85;
// Leaves room above the tallest bar for its value label
const MAX_BAR = 0.8;

export const NO_DATA = '—';

export const summarize = (session: AnalysisSession, unit: WeightUnit): ResultSummary =>
  isJump(session) ? summarizeJump(session) : summarizeSquat(session, unit);

function summarizeSquat(session: AnalysisSession, unit: WeightUnit): ResultSummary {
  const reps = session.reps ?? [];
  const velocities = reps.map((r) => r.concentricVelocity ?? 0);
  const first = velocities[0] ?? 0;
  const peak = Math.max(0, ...velocities);
  const max = peak > 0 ? peak : 1;
  const bars = reps.map((rep, i): ResultBar => {
    const v = velocities[i];
    const tone: BarTone = !rep.valid ? 'invalid' : i === 0 ? 'top' : v < first * SLOW_REP_SHARE ? 'low' : 'normal';
    return { label: v.toFixed(2), fraction: (v / max) * MAX_BAR, tone };
  });

  const fatigue = session.fatigueIndex;
  const advice =
    fatigue == null
      ? null
      : fatigue < HIGH_VELOCITY_LOSS
        ? 'Bar speed held up well — room for another set.'
        : 'Speed dropped a lot — good place to stop.';
  const note = [session.weightKg != null ? formatWeight(session.weightKg, unit) : null, advice]
    .filter(Boolean)
    .join(' · ');

  return {
    title: `Nice set! ${session.totalReps ?? reps.length} reps · Squat`,
    note,
    bars,
    stats: [
      // Backend velocity is normalised to body size (1/s)
      { label: 'Mean vel.', value: session.avgConcentricVelocity != null ? `${session.avgConcentricVelocity.toFixed(2)} 1/s` : NO_DATA },
      { label: 'Est. 1RM', value: session.estimatedRM != null ? formatWeight(session.estimatedRM, unit) : NO_DATA },
      { label: 'Vel. loss', value: fatigue != null ? `${Math.round(fatigue * 100)}%` : NO_DATA },
    ],
  };
}

function summarizeJump(session: AnalysisSession): ResultSummary {
  const jumps = session.jumps ?? [];
  const heights = jumps.map((j) => j.heightFlightCm ?? 0);
  const bestIndex = heights.reduce((best, h, i) => (best === -1 || h > heights[best] ? i : best), -1);
  const peak = Math.max(0, ...heights);
  const max = peak > 0 ? peak : 1;
  const bars = heights.map((h, i): ResultBar => ({
    label: h.toFixed(1),
    fraction: (h / max) * MAX_BAR,
    tone: i === bestIndex ? 'top' : 'normal',
  }));

  const best = session.bestJumpHeightCm;
  const bestFlight = bestIndex >= 0 ? jumps[bestIndex].flightTime : null;
  const flightMs = bestFlight != null ? Math.round(bestFlight * 1000) : best != null ? flightTimeMs(best) : null;
  return {
    title: `${session.totalJumps ?? jumps.length} jumps · Vertical jump`,
    note: 'Your best jump counts towards your record.',
    bars,
    stats: [
      { label: 'Best height', value: best != null ? `${best.toFixed(1)} cm` : NO_DATA },
      { label: 'Flight time', value: flightMs != null ? `${flightMs} ms` : NO_DATA },
      { label: 'Peak power', value: session.peakPowerW != null ? `${formatInt(session.peakPowerW)} W` : NO_DATA },
    ],
  };
}
