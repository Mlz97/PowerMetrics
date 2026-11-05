import { useQueryClient } from '@tanstack/react-query';
import { createContext, useCallback, useContext, useMemo, useRef, useState, type ReactNode } from 'react';
import { createSession, getSession, uploadVideo, type UploadParams } from '../api/client';
import { ApiError } from '../api/errors';
import { uploadProblem } from '../api/uploadRules';
import type { AnalysisSession, ExerciseType } from '../domain/types';
import { readVideoDuration } from './readVideoDuration';
import { SESSIONS_KEY } from './useSessions';

const POLL_INTERVAL_MS = 2000;
const ANALYSIS_TIMEOUT_MS = 10 * 60 * 1000;

/** Steps of one video analysis, in order. */
export type AnalysisStage = 'idle' | 'uploading' | 'analyzing' | 'done' | 'error';

export interface AnalysisState {
  stage: AnalysisStage;
  exerciseType: ExerciseType;
  fileName: string;
  /** Upload fraction (0..1); null while the stage has no measurable progress. */
  uploadProgress: number | null;
  result: AnalysisSession | null;
  errorMessage: string | null;
}

const IDLE: AnalysisState = {
  stage: 'idle',
  exerciseType: 'SQUAT',
  fileName: '',
  uploadProgress: null,
  result: null,
  errorMessage: null,
};

export const isBusy = (state: AnalysisState) => state.stage === 'uploading' || state.stage === 'analyzing';

interface AnalysisValue {
  state: AnalysisState;
  analyze: (file: File, exerciseType: ExerciseType, params: UploadParams) => void;
  clear: () => void;
}

const AnalysisContext = createContext<AnalysisValue | null>(null);

const sleep = (ms: number, signal: AbortSignal) =>
  new Promise<void>((resolve, reject) => {
    const timer = window.setTimeout(resolve, ms);
    signal.addEventListener('abort', () => {
      window.clearTimeout(timer);
      reject(new DOMException('Cancelled', 'AbortError'));
    });
  });

/** Runs a video analysis against the API: create session → upload → poll until FINISHED or ERROR. Lives above the tabs. */
export function AnalysisProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [state, setState] = useState<AnalysisState>(IDLE);
  const controller = useRef<AbortController | null>(null);

  const waitForAnalysis = useCallback(async (sessionId: number, signal: AbortSignal) => {
    const deadline = Date.now() + ANALYSIS_TIMEOUT_MS;
    while (Date.now() < deadline) {
      const session = await getSession(sessionId);
      if (session.state === 'FINISHED') return session;
      if (session.state === 'ERROR') {
        throw new Error('The video analysis failed. Check the filming tips and try again.');
      }
      await sleep(POLL_INTERVAL_MS, signal);
    }
    throw new Error('The analysis is taking too long. Try again later.');
  }, []);

  const analyze = useCallback(
    (file: File, exerciseType: ExerciseType, params: UploadParams) => {
      if (controller.current) return;
      const abort = new AbortController();
      controller.current = abort;
      setState({ ...IDLE, stage: 'uploading', exerciseType, fileName: file.name, uploadProgress: 0 });

      void (async () => {
        try {
          const problem = uploadProblem({ name: file.name, size: file.size, duration: await readVideoDuration(file) });
          if (problem) throw new Error(problem);

          const session = await createSession(exerciseType);
          // The API answers 202 and analyzes in the background
          await uploadVideo(
            session.id,
            file,
            params,
            (fraction) => setState((s) => (s.stage === 'uploading' ? { ...s, uploadProgress: fraction } : s)),
            abort.signal,
          );
          setState((s) => ({ ...s, stage: 'analyzing', uploadProgress: null }));

          const result = await waitForAnalysis(session.id, abort.signal);
          setState((s) => ({ ...s, stage: 'done', result }));
          void queryClient.invalidateQueries({ queryKey: SESSIONS_KEY });
        } catch (e) {
          if (abort.signal.aborted) return;
          const message = e instanceof ApiError || e instanceof Error ? e.message : 'Something went wrong.';
          setState((s) => ({ ...s, stage: 'error', uploadProgress: null, errorMessage: message }));
        } finally {
          if (controller.current === abort) controller.current = null;
        }
      })();
    },
    [queryClient, waitForAnalysis],
  );

  const clear = useCallback(() => {
    controller.current?.abort();
    controller.current = null;
    setState(IDLE);
  }, []);

  const value = useMemo(() => ({ state, analyze, clear }), [state, analyze, clear]);
  return <AnalysisContext.Provider value={value}>{children}</AnalysisContext.Provider>;
}

export function useAnalysis(): AnalysisValue {
  const value = useContext(AnalysisContext);
  if (!value) throw new Error('useAnalysis must be used inside AnalysisProvider');
  return value;
}
