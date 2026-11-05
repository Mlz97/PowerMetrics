const METADATA_TIMEOUT_MS = 5000;

/** Duration in seconds read from the file's metadata; null when the browser can't decode it (HEVC on many browsers). */
export function readVideoDuration(file: File): Promise<number | null> {
  return new Promise((resolve) => {
    const url = URL.createObjectURL(file);
    const video = document.createElement('video');
    const done = (duration: number | null) => {
      window.clearTimeout(timer);
      URL.revokeObjectURL(url);
      resolve(duration);
    };
    const timer = window.setTimeout(() => done(null), METADATA_TIMEOUT_MS);
    video.preload = 'metadata';
    video.onloadedmetadata = () => done(Number.isFinite(video.duration) ? video.duration : null);
    video.onerror = () => done(null);
    video.src = url;
  });
}
