import { describe, expect, it } from 'vitest';
import { apiErrorFrom } from './errors';
import { MAX_UPLOAD_BYTES, uploadProblem } from './uploadRules';

describe('API errors', () => {
  it('explains an oversized upload', () => {
    const error = apiErrorFrom(413, '{"status":413,"error":"FILE_TOO_LARGE","message":"El vídeo supera el tamaño máximo permitido"}', null);
    expect(error.status).toBe(413);
    expect(error.code).toBe('FILE_TOO_LARGE');
    expect(error.message).toBe('This video is too large. Trim it to one set and try again.');
  });

  it('handles a 413 from the proxy without a JSON body', () => {
    const error = apiErrorFrom(413, '<html>Request Entity Too Large</html>', null);
    expect(error.code).toBeNull();
    expect(error.message).toBe('This video is too large. Trim it to one set and try again.');
  });

  it('tells when to retry after hitting the rate limit', () => {
    const error = apiErrorFrom(429, '{"error":"TOO_MANY_REQUESTS"}', '1800');
    expect(error.message).toBe('Too many uploads from your connection. Try again in 30 min.');
  });

  it('rounds a short wait up to one minute', () => {
    expect(apiErrorFrom(429, '{}', '5').message).toBe('Too many uploads from your connection. Try again in 1 min.');
  });

  it('copes with a rate limit without Retry-After', () => {
    expect(apiErrorFrom(429, '', null).message).toBe('Too many uploads from your connection. Try again later.');
  });

  it('explains a busy analyzer', () => {
    const error = apiErrorFrom(503, '{"error":"ANALYSIS_BUSY"}', '60');
    expect(error.code).toBe('ANALYSIS_BUSY');
    expect(error.message).toBe('The analyzer is busy right now. Try again in a minute.');
  });

  it('separates bad parameters from bad files', () => {
    expect(apiErrorFrom(400, '{"error":"INVALID_PARAMETERS"}', null).message).toBe(
      'Check your load, bodyweight and height and try again.',
    );
    expect(apiErrorFrom(400, '{"error":"BAD_REQUEST"}', null).message).toBe(
      'That file is not a supported video. Use MP4, MOV, M4V or WEBM.',
    );
  });
});

describe('upload rules', () => {
  const ok = { name: 'IMG_0001.MOV', size: 50 * 1024 * 1024, duration: 20 };

  it('accepts an iPhone MOV within the limits', () => {
    expect(uploadProblem(ok)).toBeNull();
  });

  it('accepts a video whose duration the browser cannot read', () => {
    expect(uploadProblem({ ...ok, duration: null })).toBeNull();
  });

  it('rejects unsupported formats', () => {
    expect(uploadProblem({ ...ok, name: 'clip.avi' })).toBe('That file is not a supported video. Use MP4, MOV, M4V or WEBM.');
    expect(uploadProblem({ ...ok, name: 'clip' })).toBe('That file is not a supported video. Use MP4, MOV, M4V or WEBM.');
  });

  it('rejects empty files', () => {
    expect(uploadProblem({ ...ok, size: 0 })).toBe('That video is empty.');
  });

  it('rejects files over the upload limit', () => {
    expect(uploadProblem({ ...ok, size: MAX_UPLOAD_BYTES + 1024 * 1024 })).toBe(
      'This video is 201 MB; the limit is 200 MB. Trim it to one set.',
    );
  });

  it('rejects videos longer than a minute', () => {
    expect(uploadProblem({ ...ok, duration: 75.4 })).toBe('This video is 75 s long; the limit is 60 s. Trim it to one set.');
  });
});
