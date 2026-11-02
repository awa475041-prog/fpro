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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
                    },
                    onGoForward = {
                        if (webViewInstance?.canGoForward() == true) {
                            webViewInstance?.goForward()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebTurboApp(
    viewModel: WebTurboViewModel,
    savedWebState: Bundle?,
    onRegisterWebView: (WebView) -> Unit,
    onExecuteScript: (String) -> Unit,
    onClearCache: () -> Unit,
    onReload: () -> Unit,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit
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
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        topBar = {
            WebTurboTopBar(
                uiState = uiState,
                onGoBack = onGoBack,
                onGoForward = onGoForward,
                onReload = onReload,
                onOpenUrlDialog = { viewModel.setUrlDialogVisible(true) },
                onOpenDownloads = { viewModel.setDownloadsSheetVisible(true) },
                onOpenSettings = { viewModel.setSettingsSheetVisible(true) }
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Native Accelerated WebView
            AcceleratedWebViewContainer(
                uiState = uiState,
                savedWebState = savedWebState,
                viewModel = viewModel,
                onRegisterWebView = onRegisterWebView
            )

            // Smooth animated top loading bar
            if (uiState.isLoading) {
                LinearProgressIndicator(
                    progress = { uiState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.TopCenter)
                        .testTag("loading_progress_bar"),
                    color = CyberCyan,
                    trackColor = DarkSurfaceVariant
                )
            }

            // In-page Active video alert badge if a video is freshly captured
            AnimatedVisibility(
                visible = uiState.detectedVideos.isNotEmpty() && !uiState.showDownloadsSheet && !uiState.showVideoDownloadModal,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 24.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.setDownloadsSheetVisible(true) },
                    containerColor = CyberCyan,
                    contentColor = Color(0xFF00363D),
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                    modifier = Modifier.testTag("fab_detected_videos")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "فيديو متاح (${uiState.detectedVideos.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
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

@Composable
fun WebTurboTopBar(
    uiState: WebTurboUiState,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onReload: () -> Unit,
    onOpenUrlDialog: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .testTag("webturbo_top_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = onGoBack,
                enabled = uiState.canGoBack,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("nav_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (uiState.canGoBack) CyberCyan else TextSecondaryDark.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Forward Button
            IconButton(
                onClick = onGoForward,
                enabled = uiState.canGoForward,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("nav_forward_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (uiState.canGoForward) CyberCyan else TextSecondaryDark.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // URL & Domain Pill
            val domain = try {
                Uri.parse(uiState.currentUrl).host ?: "m.facebook.com"
            } catch (e: Exception) {
                "m.facebook.com"
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onOpenUrlDialog() }
                    .testTag("url_bar_pill"),
                shape = RoundedCornerShape(20.dp),
                color = DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secure SSL",
                        tint = NeonEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = domain,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimaryDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (uiState.blockedAdsCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonEmerald.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🛡️ ${uiState.blockedAdsCount}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Reload / Stop Button
            IconButton(
                onClick = onReload,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("nav_reload_button")
            ) {
                Icon(
                    imageVector = if (uiState.isLoading) Icons.Default.Close else Icons.Default.Refresh,
                    contentDescription = "Reload",
                    tint = TextPrimaryDark,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Downloads Tray Button
            IconButton(
                onClick = onOpenDownloads,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("nav_downloads_button")
            ) {
                BadgedBox(
                    badge = {
                        if (uiState.detectedVideos.isNotEmpty()) {
                            Badge(
                                containerColor = CyberCyan,
                                contentColor = Color(0xFF00363D)
                            ) {
                                Text(uiState.detectedVideos.size.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Downloads",
                        tint = if (uiState.detectedVideos.isNotEmpty()) CyberCyan else TextSecondaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Settings Gear ⚙️
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CyberCyan.copy(alpha = 0.12f))
                    .border(1.dp, CyberCyan.copy(alpha = 0.5f), CircleShape)
                    .clickable { onOpenSettings() }
                    .testTag("nav_settings_gear"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = CyberCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
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

                        // Always purge "Get Facebook for Android" and "Open in App" banners safely
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
