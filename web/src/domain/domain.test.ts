import { describe, expect, it } from 'vitest';
import { formatMetric, metricsForLift, metricValue, rank, type CommunityEntry } from './community';
import { historyChart, homeStats, lastFinishedSession, parseCreatedAt } from './homeStats';
import { flightTimeMs, sayersPeakPowerW } from './jumpMath';
import { canContinue, emptyDraft, toProfile, withAge, withBodyWeight, withHeight } from './profile';
import { summarize } from './resultSummary';
import { dayOfMonth, monthShort, sessionHeadline, shortDate } from './sessionText';
import { testSession } from './testSessions';
import type { JumpDetail, RepDetail } from './types';
import { formatBodyWeight, formatWeight, formatWeightNumber } from './units';

describe('units', () => {
  it('formats kg with at most one decimal', () => {
    expect(formatWeight(150, 'kg')).toBe('150 kg');
    expect(formatWeight(82.5, 'kg')).toBe('82.5 kg');
    expect(formatWeight(82.26, 'kg')).toBe('82.3 kg');
  });

  it('rounds lb to the nearest 5', () => {
    expect(formatWeight(150, 'lb')).toBe('330 lb');
    expect(formatWeight(82, 'lb')).toBe('180 lb');
  });

  it('keeps one decimal for bodyweight in lb', () => {
    expect(formatBodyWeight(82, 'lb')).toBe('180.8 lb');
    expect(formatBodyWeight(82.5, 'lb')).toBe('181.9 lb');
    expect(formatBodyWeight(82.5, 'kg')).toBe('82.5 kg');
  });

  it('formats the bare number without unit', () => {
    expect(formatWeightNumber(150, 'kg')).toBe('150');
    expect(formatWeightNumber(150, 'lb')).toBe('330');
  });
});

describe('jump math', () => {
  it('uses the Sayers equation for peak power', () => {
    expect(sayersPeakPowerW(41.2, 82)).toBe(4160);
  });

  it('derives flight time from height', () => {
    expect(flightTimeMs(41.2)).toBe(580);
  });
});

describe('profile draft', () => {
  it('needs a name and a sex to continue', () => {
    expect(canContinue({ ...emptyDraft(), name: 'Alex', sex: 'FEMALE' })).toBe(true);
    expect(canContinue({ ...emptyDraft(), name: '  ', sex: 'FEMALE' })).toBe(false);
    expect(canContinue({ ...emptyDraft(), name: 'Alex', sex: null })).toBe(false);
  });

  it('clamps the steppers', () => {
    const draft = { ...emptyDraft(), age: 13, bodyWeightKg: 35, heightCm: 120 };
    expect(withAge(draft, -1).age).toBe(13);
    expect(withBodyWeight(draft, -0.5).bodyWeightKg).toBe(35);
    expect(withHeight(draft, -1).heightCm).toBe(120);
    expect(withAge({ ...emptyDraft(), age: 99 }, 1).age).toBe(99);
    expect(withBodyWeight({ ...emptyDraft(), bodyWeightKg: 82 }, 0.5).bodyWeightKg).toBe(82.5);
  });

  it('builds a trimmed profile only when complete', () => {
    expect(toProfile({ ...emptyDraft(), name: ' Alex ', sex: 'MALE' })?.name).toBe('Alex');
    expect(toProfile({ ...emptyDraft(), name: 'Alex' })).toBeNull();
  });
});

describe('community', () => {
  const heavy: CommunityEntry = { name: 'Heavy', bodyWeightKg: 100, loadKg: 200, meanVelocity: 0.25, powerW: 2000 };
  const light: CommunityEntry = { name: 'Light', bodyWeightKg: 60, loadKg: 150 };
  const jumper: CommunityEntry = { name: 'Jumper', bodyWeightKg: 82, jumpHeightCm: 41.2 };

  it('ranks by the selected metric, highest first', () => {
    expect(rank([light, heavy], 'LOAD').map((e) => e.name)).toEqual(['Heavy', 'Light']);
    expect(rank([heavy, light], 'RELATIVE').map((e) => e.name)).toEqual(['Light', 'Heavy']);
  });

  it('derives jump metrics from the height', () => {
    expect(metricValue('HEIGHT', jumper)).toBe(41.2);
    expect(metricValue('JUMP_POWER', jumper)).toBe(4160);
    expect(metricValue('POWER_PER_KG', jumper)).toBeCloseTo(4160 / 82, 9);
    expect(metricValue('FLIGHT_TIME', jumper)).toBe(580);
  });

  it('formats every metric', () => {
    expect(formatMetric('LOAD', heavy, 'kg')).toBe('200 kg');
    expect(formatMetric('LOAD', heavy, 'lb')).toBe('440 lb');
    expect(formatMetric('RELATIVE', heavy, 'kg')).toBe('2.00×BW');
    expect(formatMetric('MEAN_VELOCITY', heavy, 'kg')).toBe('0.25 m/s');
    expect(formatMetric('POWER', heavy, 'kg')).toBe('2,000 W');
    expect(formatMetric('HEIGHT', jumper, 'kg')).toBe('41.2 cm');
    expect(formatMetric('JUMP_POWER', jumper, 'kg')).toBe('4,160 W');
    expect(formatMetric('POWER_PER_KG', jumper, 'kg')).toBe('50.7 W/kg');
    expect(formatMetric('FLIGHT_TIME', jumper, 'kg')).toBe('580 ms');
  });

  it('lists the metrics of each lift', () => {
    expect(metricsForLift('SQUAT')[0]).toBe('LOAD');
    expect(metricsForLift('VERTICAL_JUMP')[0]).toBe('HEIGHT');
    expect(metricsForLift('VERTICAL_JUMP')).toHaveLength(4);
  });
});

describe('result summary', () => {
  const rep = (n: number, v: number | null, valid = true): RepDetail => ({
    repNumber: n, valid, minHipAngle: null, minKneeAngle: null, concentricVelocity: v,
  });
  const jump = (n: number, cm: number, flight: number | null = null): JumpDetail => ({
    jumpNumber: n, flightTime: flight, heightFlightCm: cm, takeoffVelocity: null, minKneeAngle: null,
    countermovementDepthCm: null, heightDisplacementCm: null, peakPowerW: null,
  });

  it('shows reps, load and velocity loss for a squat', () => {
    const summary = summarize(
      testSession({
        totalReps: 3, validReps: 3, weightKg: 120, avgConcentricVelocity: 0.55, estimatedRM: 140, fatigueIndex: 0.12,
        reps: [rep(1, 0.6), rep(2, 0.55), rep(3, 0.5)],
      }),
      'kg',
    );

    expect(summary.title).toBe('Nice set! 3 reps · Squat');
    expect(summary.note).toBe('120 kg · Bar speed held up well — room for another set.');
    expect(summary.bars.map((b) => b.label)).toEqual(['0.60', '0.55', '0.50']);
    expect(summary.bars[0].tone).toBe('top');
    expect(summary.stats.map((s) => [s.label, s.value])).toEqual([
      ['Mean vel.', '0.55 1/s'], ['Est. 1RM', '140 kg'], ['Vel. loss', '12%'],
    ]);
  });

  it('suggests stopping after a big velocity loss', () => {
    const summary = summarize(testSession({ weightKg: 100, fatigueIndex: 0.31, totalReps: 1, reps: [rep(1, 0.4)] }), 'kg');
    expect(summary.note.endsWith('Speed dropped a lot — good place to stop.')).toBe(true);
  });

  it('gives slow and invalid reps their own tone', () => {
    const bars = summarize(testSession({ reps: [rep(1, 0.6), rep(2, 0.5), rep(3, 0.58, false)] }), 'kg').bars;
    // 0.50 is below 85% of the first rep (0.51)
    expect(bars.map((b) => b.tone)).toEqual(['top', 'low', 'invalid']);
  });

  it('highlights the best jump', () => {
    const summary = summarize(
      testSession({
        exerciseType: 'VERTICAL_JUMP', totalJumps: 3, bestJumpHeightCm: 41.2, peakPowerW: 4160,
        jumps: [jump(1, 38.5), jump(2, 41.2, 0.58), jump(3, 40.1)],
      }),
      'kg',
    );

    expect(summary.title).toBe('3 jumps · Vertical jump');
    expect(summary.bars.map((b) => b.tone)).toEqual(['normal', 'top', 'normal']);
    expect(summary.stats.map((s) => [s.label, s.value])).toEqual([
      ['Best height', '41.2 cm'], ['Flight time', '580 ms'], ['Peak power', '4,160 W'],
    ]);
  });
});

describe('home stats', () => {
  const now = new Date(2026, 9, 22, 12, 0, 0);

  it('takes squat bests from the last 30 days and compares with earlier bests', () => {
    const stats = homeStats([
      testSession({ createdAt: '2026-10-20T10:00:00', estimatedRM: 150, avgConcentricVelocity: 0.48, validReps: 3 }),
      testSession({ createdAt: '2026-10-15T10:00:00', estimatedRM: 145, avgConcentricVelocity: 0.55, validReps: 4 }),
      testSession({ createdAt: '2026-08-01T10:00:00', estimatedRM: 140, avgConcentricVelocity: 0.5, validReps: 9 }),
    ], now);

    expect(stats.squat.best1RmKg).toBe(150);
    expect(stats.squat.previousBest1RmKg).toBe(140);
    expect(stats.squat.bestVelocity).toBe(0.55);
    expect(stats.squat.previousBestVelocity).toBe(0.5);
    expect(stats.squat.validReps).toBe(7);
  });

  it('uses height and peak power for jumps', () => {
    const stats = homeStats([
      testSession({ exerciseType: 'VERTICAL_JUMP', createdAt: '2026-10-18T10:00:00', bestJumpHeightCm: 41.2, peakPowerW: 3820 }),
      testSession({ exerciseType: 'VERTICAL_JUMP', createdAt: '2026-10-13T10:00:00', bestJumpHeightCm: 40.6, peakPowerW: 3900 }),
    ], now);

    expect(stats.jump.bestHeightCm).toBe(41.2);
    expect(stats.jump.bestPeakPowerW).toBe(3900);
    expect(stats.jump.previousBestHeightCm).toBeNull();
  });

  it('ignores unfinished sessions and missing dates', () => {
    const stats = homeStats([
      testSession({ state: 'ERROR', estimatedRM: 300 }),
      testSession({ createdAt: null, estimatedRM: 250 }),
    ], now);

    expect(stats.squat.best1RmKg).toBeNull();
    expect(stats.squat.validReps).toBe(0);
  });

  it('picks the newest finished session as the last one', () => {
    const newest = testSession({ id: 2, createdAt: '2026-10-20T10:00:00' });
    const last = lastFinishedSession([
      testSession({ id: 1, createdAt: '2026-10-10T10:00:00' }),
      newest,
      testSession({ id: 3, state: 'PROCESSING', createdAt: '2026-10-21T10:00:00' }),
    ]);

    expect(last).toBe(newest);
  });

  it('charts the last eight values oldest first', () => {
    const sessions = Array.from({ length: 10 }, (_, i) =>
      testSession({ id: i + 1, createdAt: `2026-10-${String(i + 1).padStart(2, '0')}T10:00:00`, estimatedRM: 101 + i }),
    );

    expect(historyChart(sessions, 'SQUAT')).toEqual([103, 104, 105, 106, 107, 108, 109, 110]);
  });

  it('parses backend timestamps with fractions and rejects malformed ones', () => {
    expect(parseCreatedAt('2026-10-20T10:15:30.123')?.getHours()).toBe(10);
    expect(parseCreatedAt('2026-10-20')).toBeNull();
    expect(parseCreatedAt(null)).toBeNull();
  });
});

describe('session text', () => {
  const profile = { name: 'Alex', age: 28, sex: 'MALE' as const, bodyWeightKg: 82, heightCm: 178, unit: 'kg' as const };

  it('summarizes a squat by load and valid reps', () => {
    expect(sessionHeadline(testSession({ weightKg: 150, validReps: 3, totalReps: 4 }), profile)).toBe('150 kg × 3');
    expect(sessionHeadline(testSession({ totalReps: 5 }), profile)).toBe('5 reps');
  });

  it('summarizes a jump by its best height', () => {
    expect(sessionHeadline(testSession({ exerciseType: 'VERTICAL_JUMP', bestJumpHeightCm: 41.2 }), profile)).toBe('41.2 cm best');
    expect(sessionHeadline(testSession({ exerciseType: 'VERTICAL_JUMP', totalJumps: 2 }), profile)).toBe('2 jumps');
  });

  it('formats session dates', () => {
    const session = testSession({ createdAt: '2026-10-20T10:00:00' });
    expect(shortDate(session)).toBe('Oct 20');
    expect(dayOfMonth(session)).toBe('20');
    expect(monthShort(session)).toBe('OCT');
    expect(shortDate(testSession({ createdAt: null }))).toBe('—');
  });
});
