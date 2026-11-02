package com.example

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: WebTurboViewModel by viewModels()
    private var webViewInstance: WebView? = null
    private var savedWebState: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable web debugging for smooth dev inspection
        WebView.setWebContentsDebuggingEnabled(true)

        // Restore webview state if available
        if (savedInstanceState != null) {
            savedWebState = savedInstanceState.getBundle("web_bundle")
        }

        setContent {
            MyApplicationTheme {
                WebTurboApp(
                    viewModel = viewModel,
                    savedWebState = savedWebState,
                    onRegisterWebView = { wv -> webViewInstance = wv },
                    onExecuteScript = { js -> webViewInstance?.evaluateJavascript(js, null) },
                    onClearCache = {
                        webViewInstance?.clearCache(true)
                        webViewInstance?.clearFormData()
                        webViewInstance?.clearHistory()
                        CookieManager.getInstance().removeAllCookies(null)
                        Toast.makeText(this, "تم مسح الكاش والبيانات المؤقتة بنجاح!", Toast.LENGTH_SHORT).show()
                    },
                    onReload = { webViewInstance?.reload() },
                    onGoBack = {
                        if (webViewInstance?.canGoBack() == true) {
                            webViewInstance?.goBack()
                        }
                    }
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val webBundle = Bundle()
        webViewInstance?.saveState(webBundle)
        outState.putBundle("web_bundle", webBundle)
    }

    override fun onPause() {
        super.onPause()
        webViewInstance?.onPause()
    }

    override fun onResume() {
        super.onResume()
        webViewInstance?.onResume()
    }

    override fun onDestroy() {
        webViewInstance?.let { wv ->
            (wv.parent as? ViewGroup)?.removeView(wv)
            wv.stopLoading()
            wv.destroy()
        }
        webViewInstance = null
        super.onDestroy()
    }
}

@Composable
fun WebTurboApp(
    viewModel: WebTurboViewModel,
    savedWebState: Bundle?,
    onRegisterWebView: (WebView) -> Unit,
    onExecuteScript: (String) -> Unit,
    onClearCache: () -> Unit,
    onReload: () -> Unit,
    onGoBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle Hardware / Gesture Back Press
    BackHandler(enabled = uiState.canGoBack) {
        onGoBack()
    }

    // Dynamic Script Re-injection when user toggles features in real time
    LaunchedEffect(uiState.isDarkModeEnabled) {
        if (uiState.isDarkModeEnabled) {
            onExecuteScript(InjectedScripts.AMOLED_DARK_CSS)
        } else {
            onExecuteScript(InjectedScripts.REMOVE_AMOLED_CSS)
        }
    }

    LaunchedEffect(uiState.isDistractionFreeEnabled) {
        if (uiState.isDistractionFreeEnabled) {
            onExecuteScript(InjectedScripts.DISTRACTION_FREE_CSS)
        } else {
            onExecuteScript(InjectedScripts.REMOVE_DISTRACTION_FREE_CSS)
        }
    }

    LaunchedEffect(uiState.isAdBlockerEnabled) {
        if (uiState.isAdBlockerEnabled) {
            onExecuteScript(InjectedScripts.AD_BLOCKER_JS)
        }
    }

    LaunchedEffect(uiState.isVideoDownloaderEnabled) {
        if (uiState.isVideoDownloaderEnabled) {
            onExecuteScript(InjectedScripts.VIDEO_DOWNLOADER_JS)
        } else {
            onExecuteScript(InjectedScripts.REMOVE_VIDEO_DOWNLOADER_JS)
        }
    }

    // Root container with safe insets to prevent any overlap with status bar or navigation bar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Native Accelerated WebView occupying safe viewport
        AcceleratedWebViewContainer(
            uiState = uiState,
            savedWebState = savedWebState,
            viewModel = viewModel,
            onRegisterWebView = onRegisterWebView
        )

        // Smooth top loading indicator
        if (uiState.isLoading) {
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .align(Alignment.TopCenter)
                    .testTag("loading_progress_bar"),
                color = CyberCyan,
                trackColor = Color.Transparent
            )
        }

        // Floating Edge Handle for Settings ⚙️ (Docked at middle side edge to never collide with top/bottom bars)
        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                .clickable { viewModel.setSettingsSheetVisible(true) }
                .testTag("floating_settings_gear"),
            color = Color(0xEA0A1118),
            shadowElevation = 6.dp
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = CyberCyan,
                modifier = Modifier
                    .padding(horizontal = 7.dp, vertical = 10.dp)
                    .size(20.dp)
            )
        }

        // Dismissible Floating Video Download Button
        AnimatedVisibility(
            visible = uiState.isVideoDownloaderEnabled &&
                    !uiState.isFloatingDownloadBadgeDismissed &&
                    uiState.detectedVideos.isNotEmpty() &&
                    !uiState.showDownloadsSheet &&
                    !uiState.showVideoDownloadModal,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CyberCyan,
                shadowElevation = 8.dp,
                modifier = Modifier.testTag("fab_detected_videos")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                ) {
                    // Tap to open downloads modal
                    Row(
                        modifier = Modifier
                            .clickable { viewModel.setDownloadsSheetVisible(true) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            tint = Color(0xFF00363D),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "فيديو متاح (${uiState.detectedVideos.size})",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00363D),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Cancel / Dismiss button (X)
                    IconButton(
                        onClick = { viewModel.dismissFloatingDownloadBadge() },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إلغاء زر التنزيل",
                            tint = Color(0xFF00363D),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets and Dialogs
    if (uiState.showSettingsSheet) {
        SettingsBottomSheet(
            uiState = uiState,
            onDismiss = { viewModel.setSettingsSheetVisible(false) },
            onToggleAdBlocker = { viewModel.toggleAdBlocker() },
            onToggleVideoDownloader = { viewModel.toggleVideoDownloader() },
            onToggleDarkMode = { viewModel.toggleDarkMode() },
            onToggleDistractionFree = { viewModel.toggleDistractionFree() },
            onToggleDomAcceleration = { viewModel.toggleDomAcceleration() },
            onOpenUrlDialog = { viewModel.setUrlDialogVisible(true) },
            onClearCache = onClearCache,
            onReloadPage = onReload
        )
    }

    if (uiState.showDownloadsSheet) {
        VideoDownloadSheet(
            activeVideo = uiState.activeVideoToDownload ?: uiState.detectedVideos.firstOrNull(),
            detectedVideos = uiState.detectedVideos,
            downloadHistory = uiState.downloadHistory,
            onDismiss = { viewModel.setDownloadsSheetVisible(false) },
            onStartDownload = { video ->
                viewModel.startNativeDownload(video)
            }
        )
    }

    if (uiState.showVideoDownloadModal) {
        VideoDownloadSheet(
            activeVideo = uiState.activeVideoToDownload,
            detectedVideos = uiState.detectedVideos,
            downloadHistory = uiState.downloadHistory,
            onDismiss = { viewModel.setVideoDownloadModalVisible(false) },
            onStartDownload = { video ->
                viewModel.startNativeDownload(video)
            }
        )
    }

    if (uiState.showUrlDialog) {
        CustomUrlDialog(
            initialUrl = uiState.currentUrl,
            onDismiss = { viewModel.setUrlDialogVisible(false) },
            onConfirmUrl = { newUrl ->
                viewModel.loadUrl(newUrl)
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AcceleratedWebViewContainer(
    uiState: WebTurboUiState,
    savedWebState: Bundle?,
    viewModel: WebTurboViewModel,
    onRegisterWebView: (WebView) -> Unit
) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Native Hardware GPU Acceleration
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS

                // Ensure CookieManager accepts session and third-party cookies for login
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)

                // Turbo WebSettings Configuration
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    allowContentAccess = true
                    allowFileAccess = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    javaScriptCanOpenWindowsAutomatically = true
                    setSupportMultipleWindows(false)
                    mediaPlaybackRequiresUserGesture = false
                    loadsImagesAutomatically = true
                    // Standard modern Chrome mobile User-Agent for seamless login
                    userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.122 Mobile Safari/537.36"
                }

                // Register Javascript Bridge for Injection
                addJavascriptInterface(viewModel.bridge, "AndroidBridge")

                // WebChromeClient for page title and progress
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        viewModel.updatePageStatus(
                            isLoading = newProgress < 100,
                            progress = newProgress / 100f,
                            url = view?.url
                        )
                        viewModel.updateNavigationState(
                            canGoBack = view?.canGoBack() == true,
                            canGoForward = view?.canGoForward() == true
                        )
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        viewModel.updatePageStatus(
                            isLoading = false,
                            progress = 1f,
                            title = title,
                            url = view?.url
                        )
                    }
                }

                // WebViewClient with Injected Superpowers
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        viewModel.updatePageStatus(isLoading = true, progress = 0.2f, url = url)
                        viewModel.updateNavigationState(
                            canGoBack = view?.canGoBack() == true,
                            canGoForward = view?.canGoForward() == true
                        )
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        viewModel.updatePageStatus(isLoading = false, progress = 1f, url = url)
                        viewModel.updateNavigationState(
                            canGoBack = view?.canGoBack() == true,
                            canGoForward = view?.canGoForward() == true
                        )

                        // Always purge "Get Facebook for Android" and "فتح التطبيق" buttons safely
                        view?.evaluateJavascript(InjectedScripts.PURGE_APP_UPSELL_CSS, null)
                        view?.evaluateJavascript(InjectedScripts.PURGE_APP_UPSELL_JS, null)

                        // 1. Inject DOM Acceleration & Performance Timer
                        if (viewModel.uiState.value.isDomAccelerationEnabled) {
                            view?.evaluateJavascript(InjectedScripts.ACCELERATION_JS, null)
                        }

                        // 2. Inject Smart Ad & Sponsored Blocker
                        if (viewModel.uiState.value.isAdBlockerEnabled) {
                            view?.evaluateJavascript(InjectedScripts.AD_BLOCKER_JS, null)
                        }

                        // 3. Inject In-Page Video Downloader Hook
                        if (viewModel.uiState.value.isVideoDownloaderEnabled) {
                            view?.evaluateJavascript(InjectedScripts.VIDEO_DOWNLOADER_JS, null)
                        }

                        // 4. Inject AMOLED Dark Theme
                        if (viewModel.uiState.value.isDarkModeEnabled) {
                            view?.evaluateJavascript(InjectedScripts.AMOLED_DARK_CSS, null)
                        }

                        // 5. Inject Distraction-Free Styles
                        if (viewModel.uiState.value.isDistractionFreeEnabled) {
                            view?.evaluateJavascript(InjectedScripts.DISTRACTION_FREE_CSS, null)
                        }
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val uri = request?.url ?: return false
                        val urlStr = uri.toString()
                        val scheme = uri.scheme ?: return false
                        val host = uri.host ?: ""

                        // Block Google Play or intent prompts forcing Facebook App installation
                        if (urlStr.contains("com.facebook.katana") || 
                            scheme == "market" || 
                            (host.contains("play.google.com") && urlStr.contains("facebook")) ||
                            scheme == "fb" || 
                            scheme == "intent"
                        ) {
                            return true // Suppress external Facebook app prompt
                        }

                        // Allow normal http & https navigation inside the turbo webview
                        return if (scheme == "http" || scheme == "https") {
                            false
                        } else {
                            true
                        }
                    }
                }

                // Restore saved state if returning from background, or load primary URL
                if (savedWebState != null) {
                    restoreState(savedWebState)
                } else {
                    loadUrl(uiState.currentUrl)
                }

                onRegisterWebView(this)
            }
        },
        update = { webView ->
            // If the user entered a new URL through the dialog, load it
            if (webView.url != uiState.currentUrl && !uiState.isLoading && uiState.currentUrl.isNotBlank()) {
                val currentHost = try { Uri.parse(webView.url).host } catch (e: Exception) { null }
                val targetHost = try { Uri.parse(uiState.currentUrl).host } catch (e: Exception) { null }
                if (currentHost != targetHost) {
                    webView.loadUrl(uiState.currentUrl)
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .testTag("accelerated_webview")
    )
}
