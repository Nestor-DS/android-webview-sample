package com.ns.mwe

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.ns.mwe.ui.theme.MWETheme

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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen() {

    val context = LocalContext.current

    val webView = remember {
        WebView(context)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            webView.apply {

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url.toString()
                        return !url.startsWith()
                    }
                }

                addJavascriptInterface(
                    WebAppBridge(this),
                    "AndroidContext"
                )

                loadUrl()
            }
        }
    )
}
