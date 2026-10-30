import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { BrandMark, Button, Eyebrow } from '../components/atoms';

interface Slide {
  tag: string;
  title: string;
  body: string;
  image: string;
  chipKey: string;
  chipValue: string;
}

const SLIDES: Slide[] = [
  {
    tag: 'Squat',
    title: 'Squat smarter.',
    body: 'Upload a video of your set. We measure bar speed, power and velocity loss on every rep, and estimate your 1RM without maxing out.',
    image: 'ATHLETE SQUATTING, SIDE VIEW — PHOTO',
    chipKey: 'Mean velocity',
    chipValue: '0.62 m/s',
  },
  {
    tag: 'Vertical jump',
    title: 'Jump higher.',
    body: 'Film a countermovement jump and get your jump height, flight time and peak power from a single video.',
    image: 'ATHLETE MID-JUMP — PHOTO',
    chipKey: 'Jump height',
    chipValue: '41.2 cm',
  },
  {
    tag: 'Community',
    title: 'Lift together.',
    body: 'Coach your athletes from anywhere, compare PRs with the community and celebrate every new best.',
    image: 'COACH + ATHLETE — PHOTO',
    chipKey: 'New PR',
    chipValue: 'Squat 150 kg',
  },
];

export function WelcomeView({ onFinish }: { onFinish: () => void }) {
  const [index, setIndex] = useState(0);
  const slide = SLIDES[index];
  const last = index === SLIDES.length - 1;

  return (
    <div className="onboarding">
      <div className="row" style={{ justifyContent: 'space-between', padding: '12px 24px' }}>
        <BrandMark />
        <button type="button" className="link-btn secondary" style={{ color: 'var(--pm-text-secondary)', fontSize: 14 }} onClick={onFinish}>
          Skip
        </button>
      </div>
      <div className="slide-media stripes">
        <span className="placeholder-label">{slide.image}</span>
        <div className="slide-chip">
          <span className="eyebrow" style={{ opacity: 0.8, fontSize: 9 }}>
            {slide.chipKey}
          </span>
          <span className="mono" style={{ fontSize: 20 }}>
            {slide.chipValue}
          </span>
        </div>
      </div>
      <div style={{ padding: '28px 24px 20px' }} aria-live="polite">
        <Eyebrow className="accent">{slide.tag}</Eyebrow>
        <h1 className="display" style={{ fontSize: 40, margin: '10px 0 14px' }}>
          {slide.title}
        </h1>
        <p className="secondary" style={{ fontSize: 16, lineHeight: '23px', margin: 0 }}>
          {slide.body}
        </p>
        {last && (
          <p className="privacy-note">
            No account needed. Your profile stays in this browser; uploaded videos are deleted after 7 days and only this
            browser can see your sessions.
          </p>
        )}
      </div>
      <div className="row" style={{ justifyContent: 'space-between', padding: '0 24px 28px' }}>
        <div className="dots">
          {SLIDES.map((s, i) => (
            <button
              key={s.tag}
              type="button"
              className="dot"
              aria-label={`Go to slide ${i + 1}`}
              aria-current={i === index}
              onClick={() => setIndex(i)}
            />
          ))}
        </div>
        <Button style={{ width: last ? 160 : 112 }} onClick={() => (last ? onFinish() : setIndex(index + 1))}>
          {last ? 'Get started' : 'Next'}
        </Button>
      </div>
    </div>
  );
}

export function WelcomePage() {
  const navigate = useNavigate();
  return <WelcomeView onFinish={() => navigate('/profile')} />;
}
