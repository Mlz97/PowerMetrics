import type { WeightUnit } from './units';

export type Sex = 'MALE' | 'FEMALE' | 'OTHER';

export const SEX_LABELS: Record<Sex, string> = { MALE: 'Male', FEMALE: 'Female', OTHER: 'Other' };

export interface UserProfile {
  name: string;
  age: number;
  sex: Sex;
  bodyWeightKg: number;
  heightCm: number;
  unit: WeightUnit;
}

/** Profile form state; steppers clamp to the same limits as the design. */
export interface ProfileDraft {
  name: string;
  age: number;
  sex: Sex | null;
  bodyWeightKg: number;
  // Vertical jump analysis needs height to scale pixels to centimetres
  heightCm: number;
  unit: WeightUnit;
}

export const emptyDraft = (): ProfileDraft => ({
  name: '',
  age: 28,
  sex: null,
  bodyWeightKg: 82,
  heightCm: 178,
  unit: 'kg',
});

const clamp = (value: number, min: number, max: number) => Math.min(max, Math.max(min, value));

export const canContinue = (draft: ProfileDraft): boolean => draft.name.trim() !== '' && draft.sex !== null;

export const withAge = (draft: ProfileDraft, delta: number): ProfileDraft => ({
  ...draft,
  age: clamp(draft.age + delta, 13, 99),
});

export const withBodyWeight = (draft: ProfileDraft, deltaKg: number): ProfileDraft => ({
  ...draft,
  bodyWeightKg: clamp(draft.bodyWeightKg + deltaKg, 35, 200),
});

export const withHeight = (draft: ProfileDraft, deltaCm: number): ProfileDraft => ({
  ...draft,
  heightCm: clamp(draft.heightCm + deltaCm, 120, 230),
});

export function toProfile(draft: ProfileDraft): UserProfile | null {
  if (!canContinue(draft) || draft.sex === null) return null;
  return {
    name: draft.name.trim(),
    age: draft.age,
    sex: draft.sex,
    bodyWeightKg: draft.bodyWeightKg,
    heightCm: draft.heightCm,
    unit: draft.unit,
  };
}
