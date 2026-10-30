import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Card, Eyebrow, Segmented } from '../components/atoms';
import { StatTile } from '../components/molecules';
import { sampleCommunity } from '../data/sampleCommunity';
import { homeStats, lastFinishedSession, type HomeStats } from '../domain/homeStats';
import { flightTimeMs } from '../domain/jumpMath';
import type { UserProfile } from '../domain/profile';
import { NO_DATA } from '../domain/resultSummary';
import { sessionHeadline, shortDate } from '../domain/sessionText';
import { isJump, type AnalysisSession, type ExerciseType } from '../domain/types';
import { formatBodyWeight, formatInt, formatWeight, formatWeightNumber } from '../domain/units';
import { useProfile } from '../hooks/ProfileContext';
import { useSessions } from '../hooks/useSessions';

interface Tile {
  label: string;
  value: string;
  unit: string;
  delta: string;
  up: boolean;
}

/** Delta line against the best before the 30-day window. */
function delta(best: number | null, previous: number | null, format: (v: number) => string): [string, boolean] {
  if (best == null) return ['No data yet', false];
  if (previous == null) return ['First best', false];
  if (best > previous) return [`▲ ${format(best - previous)}`, true];
  if (best < previous) return [`▼ ${format(previous - best)}`, false];
  return ['— same', false];
}

function squatTiles(stats: HomeStats, profile: UserProfile): Tile[] {
  const s = stats.squat;
  const [rmDelta, rmUp] = delta(s.best1RmKg, s.previousBest1RmKg, (v) => formatWeight(v, profile.unit));
  const [velDelta, velUp] = delta(s.bestVelocity, s.previousBestVelocity, (v) => v.toFixed(2));
  const at = `at ${formatBodyWeight(profile.bodyWeightKg, profile.unit)}`;
  return [
    { label: 'Est. 1RM', value: s.best1RmKg != null ? formatWeightNumber(s.best1RmKg, profile.unit) : NO_DATA, unit: profile.unit, delta: rmDelta, up: rmUp },
    // The backend reports concentric velocity normalised to body size (1/s), not m/s
    { label: 'Best mean vel.', value: s.bestVelocity != null ? s.bestVelocity.toFixed(2) : NO_DATA, unit: '1/s', delta: velDelta, up: velUp },
    { label: 'Valid reps', value: String(s.validReps), unit: 'reps', delta: 'last 30 days', up: false },
    { label: 'Relative 1RM', value: s.best1RmKg != null ? (s.best1RmKg / profile.bodyWeightKg).toFixed(2) : NO_DATA, unit: '×BW', delta: at, up: false },
  ];
}

function jumpTiles(stats: HomeStats, profile: UserProfile): Tile[] {
  const j = stats.jump;
  const [hDelta, hUp] = delta(j.bestHeightCm, j.previousBestHeightCm, (v) => `${v.toFixed(1)} cm`);
  const [pDelta, pUp] = delta(j.bestPeakPowerW, j.previousBestPeakPowerW, (v) => `${Math.round((v / j.previousBestPeakPowerW!) * 100)}%`);
  const at = `at ${formatBodyWeight(profile.bodyWeightKg, profile.unit)}`;
  return [
    { label: 'Best height', value: j.bestHeightCm != null ? j.bestHeightCm.toFixed(1) : NO_DATA, unit: 'cm', delta: hDelta, up: hUp },
    { label: 'Flight time', value: j.bestHeightCm != null ? String(flightTimeMs(j.bestHeightCm)) : NO_DATA, unit: 'ms', delta: 'of your best jump', up: false },
    { label: 'Peak power', value: j.bestPeakPowerW != null ? formatInt(j.bestPeakPowerW) : NO_DATA, unit: 'W', delta: pDelta, up: pUp },
    { label: 'Relative power', value: j.bestPeakPowerW != null ? (j.bestPeakPowerW / profile.bodyWeightKg).toFixed(1) : NO_DATA, unit: 'W/kg', delta: at, up: false },
  ];
}

export function HomeView({
  profile,
  today,
  stats,
  lastSession,
  statsLift,
  onStatsLiftChange,
  onAnalyze,
  onHistory,
  onCommunity,
}: {
  profile: UserProfile;
  today: Date;
  stats: HomeStats;
  lastSession: AnalysisSession | null;
  statsLift: ExerciseType;
  onStatsLiftChange: (lift: ExerciseType) => void;
  onAnalyze: () => void;
  onHistory: () => void;
  onCommunity: () => void;
}) {
  const tiles = statsLift === 'SQUAT' ? squatTiles(stats, profile) : jumpTiles(stats, profile);
  const community = sampleCommunity;
  return (
    <div className="page">
      <header className="row" style={{ padding: '0 6px 6px' }}>
        <div className="stack grow" style={{ gap: 2 }}>
          <span className="secondary" style={{ fontSize: 14 }}>
            {today.toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' })}
          </span>
          <h1 className="display" style={{ fontSize: 26 }}>
            Ready to train, {profile.name}?
          </h1>
        </div>
        <Link to="/profile" className="avatar" style={{ background: 'var(--pm-text)', color: 'var(--pm-background)', textDecoration: 'none' }} aria-label="Edit profile">
          {profile.name.slice(0, 1).toUpperCase()}
        </Link>
      </header>

      <button type="button" className="hero" onClick={onAnalyze}>
        <span className="row">
          <Eyebrow className="grow">
            <span style={{ opacity: 0.85 }}>Video · VBT</span>
          </Eyebrow>
          <span className="hero-icon" aria-hidden="true">
            ↑
          </span>
        </span>
        <span className="stack" style={{ gap: 6 }}>
          <span className="display" style={{ fontSize: 30 }}>
            Analyze a video
          </span>
          <span style={{ fontSize: 15, opacity: 0.92 }}>Upload a squat or jump and get every rep measured.</span>
        </span>
      </button>

      <Card>
        <div className="row">
          <div className="stack grow" style={{ gap: 2 }}>
            <span className="card-title">Your stats</span>
            <span className="muted" style={{ fontSize: 12 }}>
              Best in the last 30 days
            </span>
          </div>
          <Segmented<ExerciseType>
            compact
            label="Stats lift"
            options={[
              { value: 'SQUAT', label: 'Squat' },
              { value: 'VERTICAL_JUMP', label: 'Jump' },
            ]}
            value={statsLift}
            onChange={onStatsLiftChange}
          />
        </div>
        <div className="grid-2">
          {tiles.map((t) => (
            <StatTile key={t.label} label={t.label} value={t.value} unit={t.unit} delta={t.delta} deltaTone={t.up ? 'up' : 'muted'} />
          ))}
        </div>
      </Card>

      <div className="grid-2">
        <Card onClick={onHistory} style={{ gap: 22 }}>
          <span className="card-title">Historic</span>
          <span className="stack" style={{ gap: 2 }}>
            <span className="muted" style={{ fontSize: 12 }}>
              Last session
            </span>
            {lastSession ? (
              <>
                <span className="mono" style={{ fontSize: 16 }}>
                  {sessionHeadline(lastSession, profile)}
                </span>
                <span className="secondary" style={{ fontSize: 13 }}>
                  {isJump(lastSession) ? 'Vertical jump' : 'Squat'} · {shortDate(lastSession)}
                </span>
              </>
            ) : (
              <>
                <span className="mono" style={{ fontSize: 16 }}>
                  No sessions yet
                </span>
                <span className="secondary" style={{ fontSize: 13 }}>
                  Analyze your first set
                </span>
              </>
            )}
          </span>
        </Card>
        <Card tint onClick={onCommunity} style={{ gap: 22 }}>
          <span className="card-title">Community PRs</span>
          <span className="stack" style={{ gap: 2 }}>
            <span className="secondary" style={{ fontSize: 12 }}>
              Your squat rank
            </span>
            <span className="mono accent-light" style={{ fontSize: 16 }}>
              #{community.yourRank('LOAD')}
            </span>
            <span className="secondary" style={{ fontSize: 13 }}>
              of {formatInt(community.memberCount('SQUAT'))} · 83 kg class
            </span>
            {community.isSample && (
              <span className="eyebrow muted" style={{ fontSize: 9 }}>
                Sample data
              </span>
            )}
          </span>
        </Card>
      </div>
    </div>
  );
}

export function HomePage({ profile }: { profile: UserProfile }) {
  const navigate = useNavigate();
  const { data: sessions = [] } = useSessions();
  const [statsLift, setStatsLift] = useState<ExerciseType>('SQUAT');
  const now = new Date();
  return (
    <HomeView
      profile={profile}
      today={now}
      stats={homeStats(sessions, now)}
      lastSession={lastFinishedSession(sessions)}
      statsLift={statsLift}
      onStatsLiftChange={setStatsLift}
      onAnalyze={() => navigate('/analyze')}
      onHistory={() => navigate('/history')}
      onCommunity={() => navigate('/community')}
    />
  );
}

/** Route wrapper: the profile is guaranteed by the onboarding guard. */
export function HomeRoute() {
  const { profile } = useProfile();
  return profile ? <HomePage profile={profile} /> : null;
}
