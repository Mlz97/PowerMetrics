import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { processedVideoUrl } from '../api/client';
import { Segmented, Stepper } from '../components/atoms';
import { ErrorCard, ProcessingCard, TipList, UploadDropZone } from '../components/molecules';
import { ResultSheet } from '../components/organisms/ResultSheet';
import type { UserProfile } from '../domain/profile';
import { summarize } from '../domain/resultSummary';
import type { ExerciseType } from '../domain/types';
import { formatWeight } from '../domain/units';
import { isBusy, useAnalysis, type AnalysisState } from '../hooks/AnalysisContext';
import { useProfile } from '../hooks/ProfileContext';

const SQUAT_TIPS = [
  'Film side-on, 2–3 m from the bar.',
  'Keep the whole plate in frame for every rep.',
  'One set per video. Trim long pauses before uploading.',
];

const JUMP_TIPS = [
  'Film side-on with your whole body in frame.',
  'Keep the phone still, ideally on a tripod or bench.',
  'Record at 60 fps or more for accurate flight time.',
];

function stageLabel(state: AnalysisState): string {
  if (state.stage === 'uploading') return 'Uploading video…';
  return state.exerciseType === 'SQUAT' ? 'Tracking every rep…' : 'Measuring flight time…';
}

export function AnalyzeView({
  state,
  lift,
  onLiftChange,
  loadKg,
  onLoadChange,
  profile,
  onFile,
  onRetry,
  onDone,
  onOpenHistory,
}: {
  state: AnalysisState;
  lift: ExerciseType;
  onLiftChange: (lift: ExerciseType) => void;
  loadKg: number;
  onLoadChange: (deltaKg: number) => void;
  profile: UserProfile;
  onFile: (file: File) => void;
  onRetry: () => void;
  onDone: () => void;
  onOpenHistory: () => void;
}) {
  const busy = isBusy(state);
  const result = state.stage === 'done' ? state.result : null;
  return (
    <div className="page">
      <h1 className="display page-title">Analyze</h1>
      <Segmented<ExerciseType>
        label="Exercise"
        disabled={busy}
        options={[
          { value: 'SQUAT', label: 'Squat' },
          { value: 'VERTICAL_JUMP', label: 'Vertical jump' },
        ]}
        value={lift}
        onChange={onLiftChange}
      />
      {lift === 'SQUAT' && (
        <Stepper
          label="load"
          value={formatWeight(loadKg, profile.unit)}
          onMinus={() => onLoadChange(-2.5)}
          onPlus={() => onLoadChange(2.5)}
          prefix={
            <span className="grow secondary" style={{ paddingLeft: 10, fontSize: 14, fontWeight: 600 }}>
              Load on the bar
            </span>
          }
        />
      )}
      {state.stage === 'error' ? (
        <ErrorCard message={state.errorMessage ?? 'Something went wrong.'} onRetry={onRetry} />
      ) : busy ? (
        <ProcessingCard
          fileName={state.fileName}
          label={stageLabel(state)}
          progress={state.uploadProgress}
          hint={state.stage === 'analyzing' ? 'This can take a minute. Keep this tab open while we work.' : null}
        />
      ) : (
        <UploadDropZone onFile={onFile} />
      )}
      <TipList tips={lift === 'SQUAT' ? SQUAT_TIPS : JUMP_TIPS} />
      {result && (
        <ResultSheet
          summary={summarize(result, profile.unit)}
          lowFps={result.lowFpsWarning === true}
          fps={result.videoFps ?? null}
          videoUrl={processedVideoUrl(result.id)}
          onDone={onDone}
          onOpenHistory={onOpenHistory}
        />
      )}
    </div>
  );
}

export function AnalyzePage({ profile }: { profile: UserProfile }) {
  const navigate = useNavigate();
  const { state, analyze, clear } = useAnalysis();
  const [lift, setLift] = useState<ExerciseType>(state.stage === 'idle' ? 'SQUAT' : state.exerciseType);
  const [loadKg, setLoadKg] = useState(120);

  return (
    <AnalyzeView
      state={state}
      lift={lift}
      onLiftChange={(next) => {
        setLift(next);
        clear();
      }}
      loadKg={loadKg}
      onLoadChange={(delta) => setLoadKg((kg) => Math.min(400, Math.max(20, kg + delta)))}
      profile={profile}
      // Jumps need bodyweight (power) and height (pixel to cm scale); squats need the load
      onFile={(file) =>
        analyze(
          file,
          lift,
          lift === 'VERTICAL_JUMP' ? { bodyWeightKg: profile.bodyWeightKg, heightCm: profile.heightCm } : { weightKg: loadKg },
        )
      }
      onRetry={clear}
      onDone={clear}
      onOpenHistory={() => {
        const analyzed = state.exerciseType;
        clear();
        navigate(`/history?lift=${analyzed}`);
      }}
    />
  );
}

export function AnalyzeRoute() {
  const { profile } = useProfile();
  return profile ? <AnalyzePage profile={profile} /> : null;
}
