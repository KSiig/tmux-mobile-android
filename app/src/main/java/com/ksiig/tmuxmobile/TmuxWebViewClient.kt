package com.ksiig.tmuxmobile

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

class TmuxWebViewClient(
    private val onError: (String) -> Unit
) : WebViewClient() {

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        view.evaluateJavascript("""
            (function() {
                var vp = document.querySelector('meta[name="viewport"]');
                if (!vp) {
                    vp = document.createElement('meta');
                    vp.name = 'viewport';
                    document.head.appendChild(vp);
                }
                vp.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
            })();
        """.trimIndent(), null)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val host = request.url.host ?: return false
        val currentHost = view.url?.let { android.net.Uri.parse(it).host }
        return host != currentHost
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        if (request.isForMainFrame) {
            onError(error.description.toString())
        }
    }

    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse
    ) {
        if (request.isForMainFrame) {
            onError("HTTP ${errorResponse.statusCode}")
        }
    }
}
