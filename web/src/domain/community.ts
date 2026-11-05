import { flightTimeMs, sayersPeakPowerW } from './jumpMath';
import type { ExerciseType } from './types';
import { formatInt, formatWeight, type WeightUnit } from './units';

/** One athlete on a community board. Squat entries carry load/velocity/power, jump entries carry height. */
export interface CommunityEntry {
  name: string;
  bodyWeightKg: number;
  loadKg?: number;
  meanVelocity?: number;
  powerW?: number;
  jumpHeightCm?: number;
}

export const initials = (entry: CommunityEntry): string =>
  entry.name
    .split(' ')
    .filter((part) => part.trim() !== '')
    .map((part) => part[0])
    .join('');

export type CommunityMetric =
  | 'LOAD'
  | 'RELATIVE'
  | 'MEAN_VELOCITY'
  | 'POWER'
  | 'HEIGHT'
  | 'JUMP_POWER'
  | 'POWER_PER_KG'
  | 'FLIGHT_TIME';

const METRICS: { metric: CommunityMetric; lift: ExerciseType; label: string }[] = [
  { metric: 'LOAD', lift: 'SQUAT', label: 'Load' },
  { metric: 'RELATIVE', lift: 'SQUAT', label: 'Relative' },
  { metric: 'MEAN_VELOCITY', lift: 'SQUAT', label: 'Mean vel.' },
  { metric: 'POWER', lift: 'SQUAT', label: 'Power' },
  { metric: 'HEIGHT', lift: 'VERTICAL_JUMP', label: 'Height' },
  { metric: 'JUMP_POWER', lift: 'VERTICAL_JUMP', label: 'Power' },
  { metric: 'POWER_PER_KG', lift: 'VERTICAL_JUMP', label: 'W/kg' },
  { metric: 'FLIGHT_TIME', lift: 'VERTICAL_JUMP', label: 'Flight time' },
];

export const metricsForLift = (lift: ExerciseType): CommunityMetric[] =>
  METRICS.filter((m) => m.lift === lift).map((m) => m.metric);

export const metricLabel = (metric: CommunityMetric): string => METRICS.find((m) => m.metric === metric)!.label;

export const metricLift = (metric: CommunityMetric): ExerciseType => METRICS.find((m) => m.metric === metric)!.lift;

export function metricValue(metric: CommunityMetric, e: CommunityEntry): number {
  const height = e.jumpHeightCm ?? 0;
  switch (metric) {
    case 'LOAD':
      return e.loadKg ?? 0;
    case 'RELATIVE':
      return (e.loadKg ?? 0) / e.bodyWeightKg;
    case 'MEAN_VELOCITY':
      return e.meanVelocity ?? 0;
    case 'POWER':
      return e.powerW ?? 0;
    case 'HEIGHT':
      return height;
    case 'JUMP_POWER':
      return sayersPeakPowerW(height, e.bodyWeightKg);
    case 'POWER_PER_KG':
      return sayersPeakPowerW(height, e.bodyWeightKg) / e.bodyWeightKg;
    case 'FLIGHT_TIME':
      return flightTimeMs(height);
  }
}

export function formatMetric(metric: CommunityMetric, e: CommunityEntry, unit: WeightUnit): string {
  const value = metricValue(metric, e);
  switch (metric) {
    case 'LOAD':
      return formatWeight(value, unit);
    case 'RELATIVE':
      return `${value.toFixed(2)}×BW`;
    case 'MEAN_VELOCITY':
      return `${value.toFixed(2)} m/s`;
    case 'POWER':
    case 'JUMP_POWER':
      return `${formatInt(value)} W`;
    case 'HEIGHT':
      return `${value.toFixed(1)} cm`;
    case 'POWER_PER_KG':
      return `${value.toFixed(1)} W/kg`;
    case 'FLIGHT_TIME':
      return `${Math.round(value)} ms`;
  }
}

export const rank = (entries: CommunityEntry[], metric: CommunityMetric): CommunityEntry[] =>
  [...entries].sort((a, b) => metricValue(metric, b) - metricValue(metric, a));
