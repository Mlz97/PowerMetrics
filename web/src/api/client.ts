import type { AnalysisSession, ExerciseType } from '../domain/types';
import { ApiError, apiErrorFrom, messageFor } from './errors';

// Same origin behind Caddy (/api is proxied to Spring); override for an API on another domain
const API_BASE = (import.meta.env.VITE_API_BASE as string | undefined) ?? '/api';

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  let response: Response;
  try {
    // The pwr_cid cookie identifies this browser's sessions
    response = await fetch(`${API_BASE}${path}`, { credentials: 'include', ...init });
  } catch {
    throw new ApiError(messageFor(0, null, null), 0);
  }
  if (!response.ok) {
    throw apiErrorFrom(response.status, await response.text(), response.headers.get('Retry-After'));
  }
  return (await response.json()) as T;
}

export const createSession = (exerciseType: ExerciseType) =>
  request<AnalysisSession>('/session', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ exerciseType }),
  });

export const listSessions = () => request<AnalysisSession[]>('/session');

export const getSession = (id: number) => request<AnalysisSession>(`/session/${id}`);

/** URL of the tracked video, served inline so a <video> element can stream it. */
export const processedVideoUrl = (sessionId: number) => `${API_BASE}/video/download/${sessionId}`;

export interface UploadParams {
  weightKg?: number | null;
  bodyWeightKg?: number | null;
  heightCm?: number | null;
}

/** Sends the video with XHR because fetch can't report upload progress. */
export function uploadVideo(
  sessionId: number,
  file: File,
  params: UploadParams,
  onProgress: (fraction: number) => void,
  signal?: AbortSignal,
): Promise<AnalysisSession> {
  const form = new FormData();
  form.append('file', file, file.name);
  for (const [key, value] of Object.entries(params)) {
    if (value != null) form.append(key, String(value));
  }

  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    xhr.open('POST', `${API_BASE}/session/${sessionId}/video`);
    xhr.withCredentials = true;
    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable) onProgress(event.loaded / event.total);
    };
    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        resolve(JSON.parse(xhr.responseText) as AnalysisSession);
      } else {
        reject(apiErrorFrom(xhr.status, xhr.responseText, xhr.getResponseHeader('Retry-After')));
      }
    };
    xhr.onerror = () => reject(new ApiError(messageFor(0, null, null), 0));
    xhr.onabort = () => reject(new DOMException('Upload cancelled', 'AbortError'));
    signal?.addEventListener('abort', () => xhr.abort());
    xhr.send(form);
  });
}
