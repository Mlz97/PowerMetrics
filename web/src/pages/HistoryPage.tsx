import { useSearchParams } from 'react-router-dom';
import { BarChart, Card } from '../components/atoms';
import { historyChart, sessionsOf } from '../domain/homeStats';
import type { UserProfile } from '../domain/profile';
import { dayOfMonth, monthShort, sessionHeadline } from '../domain/sessionText';
import { isJump, type AnalysisSession, type ExerciseType } from '../domain/types';
import { formatWeight } from '../domain/units';
import { useProfile } from '../hooks/ProfileContext';
import { useSessions } from '../hooks/useSessions';

function ProgressChart({ values, isSquat, profile }: { values: number[]; isSquat: boolean; profile: UserProfile }) {
  const floor = Math.min(...values) * 0.96;
  const range = Math.max(...values) - floor || 1;
  const latest = values[values.length - 1];
  return (
    <Card>
      <div className="row" style={{ alignItems: 'flex-end' }}>
        <span className="grow" style={{ fontWeight: 700, fontSize: 16 }}>
          {isSquat ? 'Estimated 1RM' : 'Best jump height'}
        </span>
        <span className="mono accent" style={{ fontSize: 16 }}>
          {isSquat ? formatWeight(latest, profile.unit) : `${latest.toFixed(1)} cm`}
        </span>
      </div>
      <BarChart
        bars={values.map((v, i) => ({
          fraction: (v - floor) / range,
          color: i === values.length - 1 ? 'var(--pm-accent)' : 'var(--pm-accent-soft)',
        }))}
      />
      <div className="row mono muted" style={{ fontSize: 10, fontWeight: 600 }}>
        <span className="grow">{values.length} SESSIONS AGO</span>
        <span>LATEST</span>
      </div>
    </Card>
  );
}

function SessionRow({ session, profile }: { session: AnalysisSession; profile: UserProfile }) {
  const jump = isJump(session);
  const metric = jump ? session.bestJumpHeightCm?.toFixed(1) : session.avgConcentricVelocity?.toFixed(2);
  return (
    <div className="list-row">
      <div className="stack" style={{ width: 44, alignItems: 'center' }}>
        <span className="mono" style={{ fontSize: 16 }}>
          {dayOfMonth(session)}
        </span>
        <span className="mono muted" style={{ fontSize: 10, fontWeight: 600 }}>
          {monthShort(session)}
        </span>
      </div>
      <div className="stack grow" style={{ gap: 2 }}>
        <span style={{ fontWeight: 700, fontSize: 15 }}>{jump ? 'Vertical jump' : 'Squat'}</span>
        <span className="secondary" style={{ fontSize: 13 }}>
          {jump ? `${session.totalJumps ?? 0} jumps` : sessionHeadline(session, profile)}
        </span>
      </div>
      <div className="stack" style={{ alignItems: 'flex-end', gap: 2 }}>
        <span className="mono" style={{ fontSize: 15 }}>
          {metric ?? '—'}
        </span>
        <span className="muted" style={{ fontSize: 11 }}>
          {jump ? 'cm best' : '1/s mean'}
        </span>
      </div>
    </div>
  );
}

export function HistoryView({
  sessions,
  isLoading,
  errorMessage,
  lift,
  onLiftChange,
  profile,
  onRetry,
}: {
  sessions: AnalysisSession[];
  isLoading: boolean;
  errorMessage: string | null;
  lift: ExerciseType;
  onLiftChange: (lift: ExerciseType) => void;
  profile: UserProfile;
  onRetry: () => void;
}) {
  const isSquat = lift === 'SQUAT';
  const rows = sessionsOf(sessions, lift);
  const chart = historyChart(sessions, lift);
  let body;
  if (isLoading && sessions.length === 0) {
    body = (
      <p className="muted" style={{ textAlign: 'center', padding: 32 }} role="status">
        Loading your sessions…
      </p>
    );
  } else if (errorMessage && sessions.length === 0) {
    body = (
      <Card>
        <span style={{ fontWeight: 700, fontSize: 16 }}>Couldn&apos;t load your sessions</span>
        <span className="error-text" style={{ fontSize: 13 }}>
          {errorMessage}
        </span>
        <button type="button" className="link-btn" onClick={onRetry}>
          Retry
        </button>
      </Card>
    );
  } else if (rows.length === 0) {
    body = (
      <Card>
        <span style={{ fontWeight: 700, fontSize: 16 }}>No {isSquat ? 'squat' : 'jump'} sessions yet</span>
        <span className="secondary" style={{ fontSize: 14 }}>
          Analyze a video and it will show up here.
        </span>
      </Card>
    );
  } else {
    body = (
      <>
        {chart.length > 0 && <ProgressChart values={chart} isSquat={isSquat} profile={profile} />}
        <Card flush>
          {rows.map((s) => (
            <SessionRow key={s.id} session={s} profile={profile} />
          ))}
        </Card>
      </>
    );
  }

  return (
    <div className="page">
      <h1 className="display page-title">Historic</h1>
      <div className="chips" role="group" aria-label="Lift">
        {(
          [
            ['SQUAT', 'Squat'],
            ['VERTICAL_JUMP', 'Vertical jump'],
          ] as const
        ).map(([type, label]) => (
          <button key={type} type="button" className="chip chip-solid" aria-pressed={type === lift} onClick={() => onLiftChange(type)}>
            {label}
          </button>
        ))}
      </div>
      {body}
    </div>
  );
}

export function HistoryPage({ profile }: { profile: UserProfile }) {
  const [params, setParams] = useSearchParams();
  const lift: ExerciseType = params.get('lift') === 'VERTICAL_JUMP' ? 'VERTICAL_JUMP' : 'SQUAT';
  const { data, isLoading, error, refetch } = useSessions();
  return (
    <HistoryView
      sessions={data ?? []}
      isLoading={isLoading}
      errorMessage={error ? error.message : null}
      lift={lift}
      onLiftChange={(next) => setParams({ lift: next }, { replace: true })}
      profile={profile}
      onRetry={() => void refetch()}
    />
  );
}

export function HistoryRoute() {
  const { profile } = useProfile();
  return profile ? <HistoryPage profile={profile} /> : null;
}
