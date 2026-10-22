package com.example.feperfectsquat.network

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.buffer
import okio.source

/**
 * Streams a content Uri (e.g. a gallery video) straight into the request instead of
 * loading the whole file in memory. [onProgress] receives the fraction sent (0..1).
 */
class ContentUriRequestBody(
    private val contentResolver: ContentResolver,
    private val uri: Uri,
    private val mediaType: MediaType?,
    private val onProgress: (Float) -> Unit = {}
) : RequestBody() {

    override fun contentType(): MediaType? = mediaType

    override fun contentLength(): Long =
        contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L

    // One-shot so HttpLoggingInterceptor does not buffer the whole video to log it
    override fun isOneShot(): Boolean = true

    override fun writeTo(sink: BufferedSink) {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Could not open the video")
        val total = contentLength()
        inputStream.source().buffer().use { source ->
            var sent = 0L
            while (true) {
                val read = source.read(sink.buffer, CHUNK_BYTES)
                if (read == -1L) break
                sink.emit()
                sent += read
                if (total > 0) onProgress((sent.toFloat() / total).coerceAtMost(1f))
            }
        }
    }

    private companion object {
        const val CHUNK_BYTES = 64L * 1024
    }
}
