package com.ns.mwe

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.ns.mwe.ui.theme.MWETheme

private const val LOCAL_BASE_URL = "http://1.1.1.1:1"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WebView.setWebContentsDebuggingEnabled(true)

        setContent {
            MWETheme {
                WebViewScreen()
            }
        }
    }
}
@Composable
fun WebViewScreen() {
    val context = LocalContext.current

    val webView = rememberWebView(context)

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { webView },
        update = {
            it.loadUrl(LOCAL_BASE_URL)
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            webView.destroy()
        }
    }
}

@Composable
private fun rememberWebView(context: Context): WebView {
    return remember {
        WebView(context).apply {
            configureSettings()
            webViewClient = LocalOnlyWebViewClient()
            addJavascriptInterface(
                WebAppBridge(this),
                "AndroidContext"
            )
        }
    }
}

private fun WebView.configureSettings() {
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        useWideViewPort = true
        loadWithOverviewMode = true
    }
}

private class LocalOnlyWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString().orEmpty()
        return !url.startsWith(LOCAL_BASE_URL)
    }
}