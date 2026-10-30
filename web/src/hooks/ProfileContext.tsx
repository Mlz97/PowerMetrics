import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import { loadProfile, saveProfile } from '../data/profileStorage';
import type { UserProfile } from '../domain/profile';

interface ProfileValue {
  profile: UserProfile | null;
  setProfile: (profile: UserProfile) => void;
}

const ProfileContext = createContext<ProfileValue | null>(null);

export function ProfileProvider({ children }: { children: ReactNode }) {
  const [profile, setState] = useState<UserProfile | null>(loadProfile);
  const setProfile = useCallback((next: UserProfile) => {
    saveProfile(next);
    setState(next);
  }, []);
  const value = useMemo(() => ({ profile, setProfile }), [profile, setProfile]);
  return <ProfileContext.Provider value={value}>{children}</ProfileContext.Provider>;
}

export function useProfile(): ProfileValue {
  const value = useContext(ProfileContext);
  if (!value) throw new Error('useProfile must be used inside ProfileProvider');
  return value;
}
