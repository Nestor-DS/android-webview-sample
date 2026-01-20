package com.ns.mwe

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONObject
import java.util.Locale

class WebAppBridge(private val webView: WebView) {

    @JavascriptInterface
    fun getDeviceInfo() {
        val payload = JSONObject().apply {
            put("model", Build.MODEL)
            put("androidVersion", Build.VERSION.RELEASE)
            put("language", Locale.getDefault().language)
        }

        dispatchEvent("getDeviceInfo", payload)
    }

    @JavascriptInterface
    fun sendMessage(message: String) {
        val response = when (message) {
            MSG_HOLA -> RESP_HOLI
            else -> RESP_WORD
        }

        dispatchEvent(EVENT_MESSAGE, response)
    }

    @JavascriptInterface
    fun nameComplete(firstName: String, secondName: String, lastName: String) {
        val fullName = listOf(firstName, secondName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")

        val payload = JSONObject().apply {
            put("firstName", firstName)
            put("secondName", secondName)
            put("lastName", lastName)
            put("fullName", fullName)
        }

        dispatchEvent(EVENT_NAME_COMPLETE, payload)
    }

    private fun dispatchEvent(eventName: String, payload: Any) {
        val jsPayload = when (payload) {
            is String -> JSONObject.quote(payload)
            is JSONObject -> payload.toString()
            else -> throw IllegalArgumentException("Unsupported payload type")
        }

        val js = """
            (function () {
                const evt = new CustomEvent('$eventName', { detail: $jsPayload });
                window.dispatchEvent(evt);
            })();
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(js, null)
        }
    }

    private companion object {
        const val MSG_HOLA = "Hola"
        const val RESP_HOLI = "Holi"
        const val RESP_WORD = "Word"

        const val EVENT_MESSAGE = "sendMessage"
        const val EVENT_NAME_COMPLETE = "nameComplete"
    }
}
