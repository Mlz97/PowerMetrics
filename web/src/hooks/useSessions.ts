import { useQuery } from '@tanstack/react-query';
import { listSessions } from '../api/client';

export const SESSIONS_KEY = ['sessions'] as const;

/** This browser's sessions, newest first (the API scopes them by the pwr_cid cookie). */
export const useSessions = () => useQuery({ queryKey: SESSIONS_KEY, queryFn: listSessions });
