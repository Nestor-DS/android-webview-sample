package com.ns.mwe

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONObject

class WebAppBridge(private val webView: WebView) {

    @JavascriptInterface
    fun sendMessage(message: String) {

        val response = if (message == "Hola") {
            "Holi"
        } else {
            "Word"
        }

        val escapedResponse = JSONObject.quote(response)

        val role = """
            (function(response) {
                const evt = new CustomEvent('sendMessage', { detail: response });
                window.dispatchEvent(evt);
            })($escapedResponse);
        """.trimIndent()

        Handler(Looper.getMainLooper()).post {
            webView.evaluateJavascript(role) {}
        }
    }

    @JavascriptInterface
    fun nameComplete(firstName: String, secondName: String, lastName: String) {
        val json = JSONObject().apply {
            put("firstName", firstName)
            put("secondName", secondName)
            put("lastName", lastName)
            put("fullName", listOf(firstName, secondName, lastName)
                .filter { it.isNotBlank() }
                .joinToString(" "))
        }

        val data = """
        (function(response) {
            const evt = new CustomEvent('nameComplete', { detail: response });
            window.dispatchEvent(evt);
        })(${json.toString()});
    """.trimIndent()

        webView.post {
            webView.evaluateJavascript(data, null)
        }
    }
}
