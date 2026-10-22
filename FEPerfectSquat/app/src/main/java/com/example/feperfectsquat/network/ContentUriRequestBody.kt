package com.example.feperfectsquat.network

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

/**
 * Streams a content Uri (e.g. a gallery video) straight into the request instead of
 * loading the whole file in memory.
 */
class ContentUriRequestBody(
    private val contentResolver: ContentResolver,
    private val uri: Uri,
    private val mediaType: MediaType?
) : RequestBody() {

    override fun contentType(): MediaType? = mediaType

    override fun contentLength(): Long =
        contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L

    // One-shot so HttpLoggingInterceptor does not buffer the whole video to log it
    override fun isOneShot(): Boolean = true

    override fun writeTo(sink: BufferedSink) {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("No se pudo abrir el video")
        inputStream.source().use { source -> sink.writeAll(source) }
    }
}
