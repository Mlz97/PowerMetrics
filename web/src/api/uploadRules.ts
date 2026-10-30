// Must match the API (spring.servlet.multipart.max-file-size) and the analysis service (PWR_MAX_VIDEO_SECONDS)
export const MAX_UPLOAD_BYTES = 200 * 1024 * 1024;
export const MAX_VIDEO_SECONDS = 60;
export const ACCEPTED_EXTENSIONS = ['.mp4', '.mov', '.m4v', '.webm'];

export interface VideoCandidate {
  name: string;
  size: number;
  /** Seconds, or null when the browser can't read the duration (e.g. HEVC on Chrome). */
  duration: number | null;
}

/** Problem that stops the upload before it starts, or null when the file can be sent. */
export function uploadProblem(video: VideoCandidate): string | null {
  const dot = video.name.lastIndexOf('.');
  const extension = dot >= 0 ? video.name.slice(dot).toLowerCase() : '';
  if (!ACCEPTED_EXTENSIONS.includes(extension)) {
    return 'That file is not a supported video. Use MP4, MOV, M4V or WEBM.';
  }
  if (video.size === 0) return 'That video is empty.';
  if (video.size > MAX_UPLOAD_BYTES) {
    return `This video is ${Math.round(video.size / (1024 * 1024))} MB; the limit is ${MAX_UPLOAD_BYTES / (1024 * 1024)} MB. Trim it to one set.`;
  }
  if (video.duration != null && video.duration > MAX_VIDEO_SECONDS) {
    return `This video is ${Math.round(video.duration)} s long; the limit is ${MAX_VIDEO_SECONDS} s. Trim it to one set.`;
  }
  return null;
}
