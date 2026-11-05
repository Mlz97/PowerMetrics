import type { ButtonHTMLAttributes, CSSProperties, ReactNode } from 'react';

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & { variant?: 'primary' | 'outline'; small?: boolean };

export function Button({ variant = 'primary', small = false, className = '', ...props }: ButtonProps) {
  return <button type="button" className={`btn btn-${variant} ${small ? 'btn-sm' : ''} ${className}`} {...props} />;
}

export function Card({
  children,
  tint = false,
  flush = false,
  onClick,
  className = '',
  style,
}: {
  children: ReactNode;
  tint?: boolean;
  flush?: boolean;
  onClick?: () => void;
  className?: string;
  style?: CSSProperties;
}) {
  const classes = `card ${tint ? 'card-tint' : ''} ${flush ? 'card-flush' : ''} ${className}`;
  if (onClick) {
    return (
      <button type="button" className={`${classes} card-button`} onClick={onClick} style={style}>
        {children}
      </button>
    );
  }
  return (
    <section className={classes} style={style}>
      {children}
    </section>
  );
}

export function Segmented<T extends string>({
  options,
  value,
  onChange,
  compact = false,
  disabled = false,
  label,
}: {
  options: { value: T; label: string }[];
  value: T;
  onChange: (value: T) => void;
  compact?: boolean;
  disabled?: boolean;
  label: string;
}) {
  return (
    <div className={`segmented ${compact ? 'compact' : ''}`} role="group" aria-label={label}>
      {options.map((o) => (
        <button
          key={o.value}
          type="button"
          aria-pressed={o.value === value}
          disabled={disabled}
          onClick={() => onChange(o.value)}
        >
          {o.label}
        </button>
      ))}
    </div>
  );
}

export function Stepper({
  value,
  onMinus,
  onPlus,
  label,
  prefix,
}: {
  value: string;
  onMinus: () => void;
  onPlus: () => void;
  label: string;
  prefix?: ReactNode;
}) {
  return (
    <div className="stepper">
      {prefix}
      <button type="button" className="step-btn" aria-label={`Decrease ${label}`} onClick={onMinus}>
        −
      </button>
      <output className="stepper-value" aria-live="polite">
        {value}
      </output>
      <button type="button" className="step-btn" aria-label={`Increase ${label}`} onClick={onPlus}>
        +
      </button>
    </div>
  );
}

/** Determinate when [fraction] is given, otherwise an indeterminate sweep. */
export function ProgressBar({ fraction }: { fraction: number | null }) {
  const determinate = fraction != null;
  return (
    <div
      className={`progress ${determinate ? '' : 'progress-indeterminate'}`}
      role="progressbar"
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={determinate ? Math.round(fraction * 100) : undefined}
    >
      <div className="progress-fill" style={determinate ? { width: `${fraction * 100}%` } : undefined} />
    </div>
  );
}

export function BarChart({
  bars,
  height = 90,
}: {
  bars: { fraction: number; color: string; label?: string }[];
  height?: number;
}) {
  return (
    <div className="bars" style={{ height }} aria-hidden="true">
      {bars.map((b, i) => (
        <div key={i} className="bar" style={{ height: `${Math.max(0, b.fraction) * 100}%`, background: b.color }}>
          {b.label && <span className="bar-label">{b.label}</span>}
        </div>
      ))}
    </div>
  );
}

export function BrandMark() {
  return (
    <div className="brand">
      <span className="brand-mark" />
      PWRMETRICS
    </div>
  );
}

export const Eyebrow = ({ children, className = '' }: { children: ReactNode; className?: string }) => (
  <span className={`eyebrow ${className}`}>{children}</span>
);
