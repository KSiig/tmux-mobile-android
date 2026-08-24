package com.ksiig.tmuxmobile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ksiig.tmuxmobile.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var webView: WebView

    private var serverUrl: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView = binding.webView
        configureWebView()

        serverUrl = savedInstanceState?.getString(KEY_SERVER_URL)
        if (serverUrl != null) {
            webView.loadUrl(serverUrl!!)
        } else {
            promptForUrl()
        }
    }

    private fun configureWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.webViewClient = TmuxWebViewClient { errorMessage ->
            runOnUiThread {
                Toast.makeText(
                    this,
                    getString(R.string.error_load_failed, errorMessage),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        webView.addJavascriptInterface(TmuxBridge(this), "TmuxMobileAndroid")
    }

    private fun promptForUrl() {
        val dialog = ServerUrlDialog { url ->
            serverUrl = url
            webView.loadUrl(url)
        }
        dialog.show(supportFragmentManager, "server_url")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        serverUrl?.let { outState.putString(KEY_SERVER_URL, it) }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        private const val KEY_SERVER_URL = "server_url"
    }
}
