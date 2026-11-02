package com.example

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

data class DetectedVideo(
    val id: String = System.currentTimeMillis().toString(),
    val url: String,
    val posterUrl: String? = null,
    val title: String = "Video Media",
    val timestamp: Long = System.currentTimeMillis()
)

interface WebTurboBridgeListener {
    fun onVideoDetected(video: DetectedVideo)
    fun onAdBlocked(totalCount: Int)
    fun onPageSpeed(loadTimeMs: Long)
    fun onWebLog(message: String)
}

class WebTurboBridge(private val listener: WebTurboBridgeListener) {

    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onVideoDetected(videoUrl: String, posterUrl: String?, title: String?) {
        if (videoUrl.isBlank()) return
        mainHandler.post {
            val detected = DetectedVideo(
                url = videoUrl,
                posterUrl = posterUrl?.takeIf { it.isNotBlank() },
                title = title?.takeIf { it.isNotBlank() } ?: "Video Stream"
            )
            listener.onVideoDetected(detected)
        }
    }

    @JavascriptInterface
    fun onAdBlocked(totalCount: Int) {
        mainHandler.post {
            listener.onAdBlocked(totalCount)
        }
    }

    @JavascriptInterface
    fun onPageSpeed(loadTimeMs: Long) {
        mainHandler.post {
            listener.onPageSpeed(loadTimeMs)
        }
    }

    @JavascriptInterface
    fun logMessage(msg: String) {
        mainHandler.post {
            listener.onWebLog(msg)
        }
    }
}
