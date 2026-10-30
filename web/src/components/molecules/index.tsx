import { useRef, useState, type DragEvent } from 'react';
import { ACCEPTED_EXTENSIONS } from '../../api/uploadRules';
import { initials, type CommunityEntry } from '../../domain/community';
import { Button, Card, ProgressBar } from '../atoms';

export function StatTile({
  label,
  value,
  unit,
  delta,
  deltaTone = 'muted',
}: {
  label: string;
  value: string;
  unit: string;
  delta: string;
  deltaTone?: 'muted' | 'up';
}) {
  return (
    <div className="stat-tile">
      <span className="stat-label">{label}</span>
      <span>
        <span className="stat-value">{value}</span>
        <span className="stat-unit">{unit}</span>
      </span>
      <span className={`stat-delta ${deltaTone === 'up' ? 'accent-light' : 'muted'}`}>{delta}</span>
    </div>
  );
}

/** Click or drop a video; the file input accepts the same formats as the API. */
export function UploadDropZone({ onFile, disabled }: { onFile: (file: File) => void; disabled?: boolean }) {
  const input = useRef<HTMLInputElement>(null);
  const [dragging, setDragging] = useState(false);
  const onDrop = (event: DragEvent) => {
    event.preventDefault();
    setDragging(false);
    const file = event.dataTransfer.files[0];
    if (file && !disabled) onFile(file);
  };
  return (
    <>
      <button
        type="button"
        className={`dropzone ${dragging ? 'dragging' : ''}`}
        disabled={disabled}
        onClick={() => input.current?.click()}
        onDragOver={(e) => {
          e.preventDefault();
          setDragging(true);
        }}
        onDragLeave={() => setDragging(false)}
        onDrop={onDrop}
      >
        <span className="dropzone-icon" aria-hidden="true">
          ↑
        </span>
        <span style={{ fontWeight: 700, fontSize: 18 }}>Upload a video</span>
        <span className="secondary" style={{ fontSize: 14 }}>
          MP4 or MOV · up to 60 s · one set per video
        </span>
      </button>
      <input
        ref={input}
        type="file"
        accept={['video/*', ...ACCEPTED_EXTENSIONS].join(',')}
        hidden
        onChange={(e) => {
          const file = e.target.files?.[0];
          e.target.value = '';
          if (file) onFile(file);
        }}
      />
    </>
  );
}

export function ProcessingCard({
  fileName,
  label,
  progress,
  hint,
}: {
  fileName: string;
  label: string;
  progress: number | null;
  hint: string | null;
}) {
  return (
    <Card>
      <div className="stripes" style={{ height: 150, borderRadius: 14, display: 'grid', placeItems: 'center' }}>
        <span className="placeholder-label">{fileName}</span>
      </div>
      <div className="row" style={{ alignItems: 'flex-end' }}>
        <span className="grow" style={{ fontWeight: 700, fontSize: 15 }}>
          {label}
        </span>
        {progress != null && <span className="mono accent">{Math.round(progress * 100)}%</span>}
      </div>
      <ProgressBar fraction={progress} />
      {hint && (
        <span className="muted" style={{ fontSize: 13 }}>
          {hint}
        </span>
      )}
    </Card>
  );
}

export function ErrorCard({ message, onRetry }: { message: string; onRetry: () => void }) {
  return (
    <Card>
      <span className="card-title">We couldn&apos;t analyze that video</span>
      <span className="error-text" role="alert" style={{ fontSize: 14 }}>
        {message}
      </span>
      <Button small onClick={onRetry}>
        Try again
      </Button>
    </Card>
  );
}

export function TipList({ tips }: { tips: string[] }) {
  return (
    <div className="stack" style={{ gap: 12, padding: '8px 6px 0' }}>
      <span className="eyebrow muted">Filming tips</span>
      {tips.map((tip, i) => (
        <div key={tip} className="row" style={{ alignItems: 'flex-start' }}>
          <span className="mono accent" style={{ fontSize: 12, paddingTop: 2 }}>
            {String(i + 1).padStart(2, '0')}
          </span>
          <span style={{ fontSize: 15, lineHeight: '21px' }}>{tip}</span>
        </div>
      ))}
    </div>
  );
}

export function BoardRow({
  rank,
  highlight,
  entry,
  main,
  secondary,
}: {
  rank: number;
  highlight: boolean;
  entry: CommunityEntry;
  main: string;
  secondary: string;
}) {
  return (
    <div className="list-row" style={{ padding: '12px 16px', gap: 12 }}>
      <span className={`mono ${highlight ? 'accent' : 'muted'}`} style={{ width: 18, fontSize: 14 }}>
        {rank}
      </span>
      <span className="avatar" style={{ width: 36, height: 36, background: 'var(--pm-canvas)', fontSize: 13 }}>
        {initials(entry)}
      </span>
      <div className="stack grow" style={{ gap: 3 }}>
        <span style={{ fontWeight: 700, fontSize: 15 }}>{entry.name}</span>
        <span className="mono muted" style={{ fontSize: 11, fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
          {secondary}
        </span>
      </div>
      <span className="mono" style={{ fontSize: 16 }}>
        {main}
      </span>
    </div>
  );
}
