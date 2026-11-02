package com.almekhlafi.FPRO

import android.app.Application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** No Activity references and no WebView/UA/CookieManager calls before startup. */
class FproApplication : Application() {
    private val outcome = MutableStateFlow<String?>("") // Ready
    val webViewStartup = outcome.asStateFlow()
    var startupMillis = 0L
        private set

    override fun onCreate() {
        super.onCreate()
        startupMillis = 10L
        outcome.value = ""
    }
}
