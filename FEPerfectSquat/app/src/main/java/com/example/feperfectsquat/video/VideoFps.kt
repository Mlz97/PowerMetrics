package com.example.feperfectsquat.video

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri

// Por debajo de este valor la altura del salto puede tener un error de varios centímetros
const val MIN_RECOMMENDED_FPS = 60f

/** Lee los FPS de la pista de vídeo; devuelve null si el fichero no los declara. */
fun readVideoFps(context: Context, uri: Uri): Float? = runCatching {
    val extractor = MediaExtractor()
    try {
        extractor.setDataSource(context, uri, null)
        val format = (0 until extractor.trackCount)
            .map { extractor.getTrackFormat(it) }
            .firstOrNull { it.getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true }
            ?.takeIf { it.containsKey(MediaFormat.KEY_FRAME_RATE) }
        format?.let {
            runCatching { it.getInteger(MediaFormat.KEY_FRAME_RATE).toFloat() }
                .getOrElse { _ -> it.getFloat(MediaFormat.KEY_FRAME_RATE) }
        }
    } finally {
        extractor.release()
    }
}.getOrNull()
