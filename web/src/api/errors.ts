/** Error code from the API's ErrorResponse body ("FILE_TOO_LARGE", "ANALYSIS_BUSY", ...). */
export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code: string | null = null,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

const minutes = (seconds: number) => Math.max(1, Math.ceil(seconds / 60));

/** User-facing message for a failed API call; the backend's own messages are not shown because they are in Spanish. */
export function messageFor(status: number, code: string | null, retryAfterSeconds: number | null): string {
  switch (status) {
    case 0:
      return "Can't reach PwrMetrics. Check your connection and try again.";
    case 400:
      return code === 'INVALID_PARAMETERS'
        ? 'Check your load, bodyweight and height and try again.'
        : 'That file is not a supported video. Use MP4, MOV, M4V or WEBM.';
    case 404:
      return 'That session no longer exists.';
    case 409:
      return 'This session already has a video. Start a new analysis.';
    case 413:
      return 'This video is too large. Trim it to one set and try again.';
    case 429:
      return retryAfterSeconds != null
        ? `Too many uploads from your connection. Try again in ${minutes(retryAfterSeconds)} min.`
        : 'Too many uploads from your connection. Try again later.';
    case 503:
      return 'The analyzer is busy right now. Try again in a minute.';
    default:
      return 'Something went wrong on our side. Try again later.';
  }
}

export function apiErrorFrom(status: number, body: string, retryAfter: string | null): ApiError {
  let code: string | null = null;
  try {
    const parsed = JSON.parse(body) as { error?: unknown };
    code = typeof parsed.error === 'string' ? parsed.error : null;
  } catch {
    // Caddy or the browser may answer with a non-JSON body (e.g. 413 from the proxy)
  }
  const seconds = retryAfter != null && /^\d+$/.test(retryAfter) ? Number(retryAfter) : null;
  return new ApiError(messageFor(status, code, seconds), status, code);
}
