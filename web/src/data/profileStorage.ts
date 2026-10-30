import type { Sex, UserProfile } from '../domain/profile';

const KEY = 'pwr.profile';
const SEXES: Sex[] = ['MALE', 'FEMALE', 'OTHER'];

/** The onboarding profile lives in this browser only; there are no user accounts on the backend yet. */
export function loadProfile(): UserProfile | null {
  try {
    const raw = window.localStorage.getItem(KEY);
    if (!raw) return null;
    const p = JSON.parse(raw) as Partial<UserProfile>;
    if (typeof p.name !== 'string' || !p.name.trim() || !SEXES.includes(p.sex as Sex)) return null;
    return {
      name: p.name,
      sex: p.sex as Sex,
      age: typeof p.age === 'number' ? p.age : 28,
      bodyWeightKg: typeof p.bodyWeightKg === 'number' ? p.bodyWeightKg : 82,
      heightCm: typeof p.heightCm === 'number' ? p.heightCm : 178,
      unit: p.unit === 'lb' ? 'lb' : 'kg',
    };
  } catch {
    // Private mode or blocked storage: onboarding runs again
    return null;
  }
}

export function saveProfile(profile: UserProfile): void {
  try {
    window.localStorage.setItem(KEY, JSON.stringify(profile));
  } catch {
    // The profile still works for this visit
  }
}
