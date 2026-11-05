const GRAVITY = 9.81;

/** Sayers equation: peak power (W) from jump height and bodyweight. */
export const sayersPeakPowerW = (jumpHeightCm: number, bodyWeightKg: number): number =>
  Math.round(60.7 * jumpHeightCm + 45.3 * bodyWeightKg - 2055);

/** Flight time (ms) of a jump of the given height: t = 2 * sqrt(2h / g). */
export const flightTimeMs = (jumpHeightCm: number): number =>
  Math.round(2 * Math.sqrt((2 * jumpHeightCm) / 100 / GRAVITY) * 1000);

// Below this the jump height can be off by several centimetres
export const MIN_RECOMMENDED_FPS = 60;
