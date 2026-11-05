import { useState } from 'react';
import { Card, Segmented } from '../components/atoms';
import { BoardRow } from '../components/molecules';
import { sampleCommunity, type CommunityRepository } from '../data/sampleCommunity';
import {
  formatMetric,
  metricLabel,
  metricLift,
  metricsForLift,
  rank,
  type CommunityEntry,
  type CommunityMetric,
} from '../domain/community';
import type { UserProfile } from '../domain/profile';
import type { ExerciseType } from '../domain/types';
import { useProfile } from '../hooks/ProfileContext';

export function CommunityView({
  repository,
  profile,
  lift,
  metric,
  onLiftChange,
  onMetricChange,
}: {
  repository: CommunityRepository;
  profile: UserProfile;
  lift: ExerciseType;
  metric: CommunityMetric;
  onLiftChange: (lift: ExerciseType) => void;
  onMetricChange: (metric: CommunityMetric) => void;
}) {
  const metrics = metricsForLift(lift);
  const selected = metricLift(metric) === lift ? metric : metrics[0];
  const others = (e: CommunityEntry) =>
    metrics
      .filter((m) => m !== selected)
      .map((m) => formatMetric(m, e, profile.unit))
      .join(' · ');
  const board = rank(repository.board(lift), selected);
  const you = repository.you(lift, profile.bodyWeightKg);

  return (
    <div className="page">
      <h1 className="display page-title">Community PRs</h1>
      {repository.isSample && (
        <div className="notice">
          <span className="eyebrow accent-light" style={{ fontSize: 10 }}>
            Sample data
          </span>
          Community rankings aren&apos;t live yet. These boards are an example.
        </div>
      )}
      <Segmented<ExerciseType>
        label="Lift"
        options={[
          { value: 'SQUAT', label: 'Squat' },
          { value: 'VERTICAL_JUMP', label: 'Vertical jump' },
        ]}
        value={lift}
        onChange={onLiftChange}
      />
      <div className="stack" style={{ gap: 8, padding: '2px 0' }}>
        <span className="eyebrow muted" style={{ fontSize: 10, padding: '0 6px' }}>
          Rank by
        </span>
        <div className="chips" role="group" aria-label="Rank by">
          {metrics.map((m) => (
            <button key={m} type="button" className="chip" aria-pressed={m === selected} onClick={() => onMetricChange(m)}>
              {metricLabel(m)}
            </button>
          ))}
        </div>
      </div>
      <span className="secondary" style={{ fontSize: 13, padding: '0 6px' }}>
        {repository.context(lift)}
      </span>
      <Card flush>
        {board.map((entry, i) => (
          <BoardRow
            key={entry.name}
            rank={i + 1}
            highlight={i < 3}
            entry={entry}
            main={formatMetric(selected, entry, profile.unit)}
            secondary={others(entry)}
          />
        ))}
      </Card>
      <div className="row card-tint" style={{ borderRadius: 14, border: '1px solid var(--pm-line)', padding: '14px 16px' }}>
        <span className="mono accent-light" style={{ width: 44, fontSize: 14 }}>
          #{repository.yourRank(selected)}
        </span>
        <div className="stack grow" style={{ gap: 3 }}>
          <span style={{ fontWeight: 700, fontSize: 15 }}>You</span>
          <span className="mono secondary" style={{ fontSize: 11, fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
            {others(you)}
          </span>
        </div>
        <span className="mono" style={{ fontSize: 16 }}>
          {formatMetric(selected, you, profile.unit)}
        </span>
      </div>
    </div>
  );
}

export function CommunityPage({ profile }: { profile: UserProfile }) {
  const [lift, setLift] = useState<ExerciseType>('SQUAT');
  const [metric, setMetric] = useState<CommunityMetric>('LOAD');
  return (
    <CommunityView
      repository={sampleCommunity}
      profile={profile}
      lift={lift}
      metric={metric}
      onLiftChange={(next) => {
        setLift(next);
        setMetric(metricsForLift(next)[0]);
      }}
      onMetricChange={setMetric}
    />
  );
}

export function CommunityRoute() {
  const { profile } = useProfile();
  return profile ? <CommunityPage profile={profile} /> : null;
}
