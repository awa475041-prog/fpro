package com.example

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DownloadItem(
    val id: Long,
    val title: String,
    val url: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class WebTurboUiState(
    val currentUrl: String = "https://m.facebook.com",
    val pageTitle: String = "Facebook",
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    // Injected Superpowers
    val isAdBlockerEnabled: Boolean = true,
    val isVideoDownloaderEnabled: Boolean = true,
    val isDarkModeEnabled: Boolean = false,
    val isDistractionFreeEnabled: Boolean = false,
    val isDomAccelerationEnabled: Boolean = true,
    // Telemetry & Stats
    val blockedAdsCount: Int = 0,
    val pageLoadTimeMs: Long = 260L,
    val detectedVideos: List<DetectedVideo> = emptyList(),
    val activeVideoToDownload: DetectedVideo? = null,
    val downloadHistory: List<DownloadItem> = emptyList(),
    // Modals
    val showSettingsSheet: Boolean = false,
    val showDownloadsSheet: Boolean = false,
    val showUrlDialog: Boolean = false,
    val showVideoDownloadModal: Boolean = false
)

class WebTurboViewModel(application: Application) : AndroidViewModel(application), WebTurboBridgeListener {

    private val _uiState = MutableStateFlow(WebTurboUiState())
    val uiState: StateFlow<WebTurboUiState> = _uiState.asStateFlow()

    val bridge = WebTurboBridge(this)

    // Bridge callbacks
    override fun onVideoDetected(video: DetectedVideo) {
        _uiState.update { state ->
            val updatedList = if (state.detectedVideos.any { it.url == video.url }) {
                state.detectedVideos
            } else {
                listOf(video) + state.detectedVideos
            }
            state.copy(
                detectedVideos = updatedList,
                activeVideoToDownload = video,
                showVideoDownloadModal = true
            )
        }
    }

    override fun onAdBlocked(totalCount: Int) {
        _uiState.update { it.copy(blockedAdsCount = totalCount) }
    }

    override fun onPageSpeed(loadTimeMs: Long) {
        if (loadTimeMs in 50..15000) {
            _uiState.update { it.copy(pageLoadTimeMs = loadTimeMs) }
        }
    }

    override fun onWebLog(message: String) {
        // Safe logging hook
    }

    // Settings Toggles
    fun toggleAdBlocker() {
        _uiState.update { it.copy(isAdBlockerEnabled = !it.isAdBlockerEnabled) }
    }

    fun toggleVideoDownloader() {
        _uiState.update { it.copy(isVideoDownloaderEnabled = !it.isVideoDownloaderEnabled) }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkModeEnabled = !it.isDarkModeEnabled) }
    }

    fun toggleDistractionFree() {
        _uiState.update { it.copy(isDistractionFreeEnabled = !it.isDistractionFreeEnabled) }
    }

    fun toggleDomAcceleration() {
        _uiState.update { it.copy(isDomAccelerationEnabled = !it.isDomAccelerationEnabled) }
    }

    // Navigation and Page Status
    fun updatePageStatus(isLoading: Boolean, progress: Float, title: String? = null, url: String? = null) {
        _uiState.update { state ->
            state.copy(
                isLoading = isLoading,
                progress = progress,
                pageTitle = title?.takeIf { it.isNotBlank() } ?: state.pageTitle,
                currentUrl = url?.takeIf { it.isNotBlank() } ?: state.currentUrl
            )
        }
    }

    fun updateNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.update { it.copy(canGoBack = canGoBack, canGoForward = canGoForward) }
    }

    fun loadUrl(newUrl: String) {
        var formatted = newUrl.trim()
        if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
            formatted = if (formatted.contains(".") && !formatted.contains(" ")) {
                "https://$formatted"
            } else {
                "https://www.google.com/search?q=" + Uri.encode(formatted)
            }
        }
        _uiState.update {
            it.copy(
                currentUrl = formatted,
                isLoading = true,
                showUrlDialog = false
            )
        }
    }

    // Modal Visibility Handlers
    fun setSettingsSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(showSettingsSheet = visible) }
    }

    fun setDownloadsSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(showDownloadsSheet = visible) }
    }

    fun setUrlDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showUrlDialog = visible) }
    }

    fun setVideoDownloadModalVisible(visible: Boolean, video: DetectedVideo? = null) {
        _uiState.update {
            it.copy(
                showVideoDownloadModal = visible,
                activeVideoToDownload = video ?: if (visible) it.activeVideoToDownload else null
            )
        }
    }

    // Trigger Native Android DownloadManager
    fun startNativeDownload(video: DetectedVideo) {
        val context = getApplication<Application>()
        try {
            val uri = Uri.parse(video.url)
            val request = DownloadManager.Request(uri).apply {
                val safeTitle = video.title.replace(Regex("[^a-zA-Z0-9_\\-\\s]"), "").trim().take(30)
                val fileName = "WebTurbo_${safeTitle.ifEmpty { "Video" }}_${System.currentTimeMillis()}.mp4"
                setTitle("WebTurbo: $safeTitle")
                setDescription("جاري تنزيل الفيديو فائق الدقة...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = manager.enqueue(request)

            val downloadItem = DownloadItem(
                id = downloadId,
                title = video.title,
                url = video.url,
                status = "جاري التنزيل"
            )

            _uiState.update {
                it.copy(
                    downloadHistory = listOf(downloadItem) + it.downloadHistory,
                    showVideoDownloadModal = false
                )
            }

            Toast.makeText(context, "🚀 بدأ تنزيل الفيديو في مجلد التنزيلات!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "تنبيه: " + (e.message ?: "فشل بدء التنزيل المباشر"), Toast.LENGTH_LONG).show()
        }
    }

    fun clearStats() {
        _uiState.update { it.copy(blockedAdsCount = 0) }
    }
}
