import { useEffect, useState } from 'react';
import { MIN_RECOMMENDED_FPS } from '../../domain/jumpMath';
import type { BarTone, ResultSummary } from '../../domain/resultSummary';
import { BarChart, Button } from '../atoms';

const TONE_COLORS: Record<BarTone, string> = {
  top: 'var(--pm-accent)',
  normal: 'var(--pm-accent-mid)',
  low: 'var(--pm-accent-soft)',
  invalid: 'var(--pm-handle)',
};

/** Bottom sheet with the metrics of a finished analysis and its tracked video. */
export function ResultSheet({
  summary,
  lowFps,
  fps,
  videoUrl,
  onDone,
  onOpenHistory,
}: {
  summary: ResultSummary;
  lowFps: boolean;
  fps: number | null;
  videoUrl: string;
  onDone: () => void;
  onOpenHistory: () => void;
}) {
  const [showVideo, setShowVideo] = useState(false);
  const [videoFailed, setVideoFailed] = useState(false);

  useEffect(() => {
    const onKey = (event: KeyboardEvent) => event.key === 'Escape' && onDone();
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onDone]);

  return (
    <div className="scrim" onClick={onDone}>
      <div className="sheet" role="dialog" aria-modal="true" aria-labelledby="result-title" onClick={(e) => e.stopPropagation()}>
        <span className="sheet-handle" />
        <div className="stack" style={{ gap: 4 }}>
          <h2 id="result-title" className="display" style={{ fontSize: 22 }}>
            {summary.title}
          </h2>
          <span className="secondary" style={{ fontSize: 14 }}>
            {summary.note}
          </span>
          {lowFps && (
            <span className="accent-light" style={{ fontSize: 13 }}>
              Filmed at {fps != null ? Math.round(fps) : '?'} fps: record at {MIN_RECOMMENDED_FPS} fps or more for an accurate height.
            </span>
          )}
        </div>
        {summary.bars.length > 0 && (
          <div style={{ paddingTop: 16 }}>
            <BarChart bars={summary.bars.map((b) => ({ fraction: b.fraction, color: TONE_COLORS[b.tone], label: b.label }))} />
          </div>
        )}
        <div className="row" style={{ gap: 8, alignItems: 'stretch' }}>
          {summary.stats.map((stat) => (
            <div key={stat.label} className="stat-tile grow" style={{ padding: 12 }}>
              <span className="stat-label" style={{ fontSize: 11 }}>
                {stat.label}
              </span>
              <span className="mono" style={{ fontSize: 16 }}>
                {stat.value}
              </span>
            </div>
          ))}
        </div>
        {/* The tracked video is a bonus: if it fails to load the metrics stay */}
        {!videoFailed &&
          (showVideo ? (
            <video className="tracked-video" src={videoUrl} controls autoPlay playsInline onError={() => setVideoFailed(true)} />
          ) : (
            <button type="button" className="link-btn" onClick={() => setShowVideo(true)}>
              ▶&nbsp; Watch tracked video
            </button>
          ))}
        <div className="row" style={{ gap: 10 }}>
          <Button variant="outline" small onClick={onDone} style={{ flex: 1 }}>
            Done
          </Button>
          <Button small onClick={onOpenHistory} style={{ flex: 2 }}>
            See in Historic
          </Button>
        </div>
      </div>
    </div>
  );
}
