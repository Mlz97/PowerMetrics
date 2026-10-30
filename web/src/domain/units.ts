export type WeightUnit = 'kg' | 'lb';

const LB_PER_KG = 2.20462;

/** Weight in the user's unit: kg with at most one decimal, lb rounded to the nearest 5. */
export function formatWeightNumber(kg: number, unit: WeightUnit): string {
  if (unit === 'lb') return String(Math.round((kg * LB_PER_KG) / 5) * 5);
  const rounded = Math.round(kg * 10) / 10;
  return Number.isInteger(rounded) ? String(rounded) : rounded.toFixed(1);
}

export const formatWeight = (kg: number, unit: WeightUnit): string => `${formatWeightNumber(kg, unit)} ${unit}`;

/** Bodyweight with one decimal in either unit, so each 0.5 kg step stays visible in lb. */
export const formatBodyWeight = (kg: number, unit: WeightUnit): string =>
  unit === 'kg' ? formatWeight(kg, unit) : `${(kg * LB_PER_KG).toFixed(1)} lb`;

/** "2,480" style grouping, as Kotlin's "%,d" with Locale.US. */
export const formatInt = (value: number): string => Math.round(value).toLocaleString('en-US');
