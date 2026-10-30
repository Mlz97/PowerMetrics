import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Segmented, Stepper } from '../components/atoms';
import {
  canContinue,
  emptyDraft,
  SEX_LABELS,
  toProfile,
  withAge,
  withBodyWeight,
  withHeight,
  type ProfileDraft,
  type Sex,
} from '../domain/profile';
import { formatBodyWeight, type WeightUnit } from '../domain/units';
import { useProfile } from '../hooks/ProfileContext';

/** "Let's get to know you": name, age, units, sex, bodyweight and height. */
export function ProfileView({
  draft,
  onChange,
  onBack,
  onContinue,
}: {
  draft: ProfileDraft;
  onChange: (update: (d: ProfileDraft) => ProfileDraft) => void;
  onBack: () => void;
  onContinue: () => void;
}) {
  return (
    <div className="onboarding">
      <div style={{ padding: '12px 24px 0' }}>
        <button type="button" className="back-btn" aria-label="Back" onClick={onBack}>
          ←
        </button>
      </div>
      <form
        className="grow"
        style={{ padding: '28px 24px 0', overflowY: 'auto' }}
        onSubmit={(e) => {
          e.preventDefault();
          if (canContinue(draft)) onContinue();
        }}
      >
        <h1 className="display" style={{ fontSize: 30 }}>
          Let&apos;s get to know you
        </h1>
        <p className="secondary" style={{ fontSize: 15, lineHeight: '22px', margin: '10px 0 4px' }}>
          A few basics so we can calculate your power output accurately.
        </p>

        <label className="field-label" htmlFor="name">
          First name
        </label>
        <input
          id="name"
          className="text-input"
          placeholder="Alex"
          autoComplete="given-name"
          autoCapitalize="words"
          value={draft.name}
          onChange={(e) => {
            const name = e.target.value;
            onChange((d) => ({ ...d, name }));
          }}
        />

        <div className="grid-2">
          <div>
            <span className="field-label">Age</span>
            <Stepper value={String(draft.age)} label="age" onMinus={() => onChange((d) => withAge(d, -1))} onPlus={() => onChange((d) => withAge(d, 1))} />
          </div>
          <div>
            <span className="field-label">Units</span>
            <Segmented<WeightUnit>
              label="Units"
              options={[
                { value: 'kg', label: 'kg' },
                { value: 'lb', label: 'lb' },
              ]}
              value={draft.unit}
              onChange={(unit) => onChange((d) => ({ ...d, unit }))}
            />
          </div>
        </div>

        <span className="field-label" id="sex-label">
          Sex
        </span>
        <div className="row" role="radiogroup" aria-labelledby="sex-label" style={{ gap: 8 }}>
          {(Object.keys(SEX_LABELS) as Sex[]).map((sex) => (
            <button
              key={sex}
              type="button"
              role="radio"
              aria-checked={draft.sex === sex}
              className="option"
              onClick={() => onChange((d) => ({ ...d, sex }))}
            >
              {SEX_LABELS[sex]}
            </button>
          ))}
        </div>

        <span className="field-label">Bodyweight</span>
        <Stepper
          value={formatBodyWeight(draft.bodyWeightKg, draft.unit)}
          label="bodyweight"
          onMinus={() => onChange((d) => withBodyWeight(d, -0.5))}
          onPlus={() => onChange((d) => withBodyWeight(d, 0.5))}
        />

        <span className="field-label">Height</span>
        <Stepper
          value={`${Math.round(draft.heightCm)} cm`}
          label="height"
          onMinus={() => onChange((d) => withHeight(d, -1))}
          onPlus={() => onChange((d) => withHeight(d, 1))}
        />
        <p className="muted" style={{ fontSize: 12, margin: '8px 0 16px' }}>
          Used to measure jump depth in your videos.
        </p>
        <button type="submit" hidden />
      </form>
      <div style={{ padding: '16px 24px 20px' }}>
        <Button style={{ width: '100%' }} disabled={!canContinue(draft)} onClick={onContinue}>
          Continue
        </Button>
      </div>
    </div>
  );
}

export function ProfilePage() {
  const navigate = useNavigate();
  const { profile, setProfile } = useProfile();
  const [draft, setDraft] = useState<ProfileDraft>(() => (profile ? { ...profile } : emptyDraft()));

  return (
    <ProfileView
      draft={draft}
      onChange={(update) => setDraft(update)}
      onBack={() => navigate(-1)}
      onContinue={() => {
        const next = toProfile(draft);
        if (next) {
          setProfile(next);
          navigate('/', { replace: true });
        }
      }}
    />
  );
}
