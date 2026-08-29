package com.ksiig.tmuxmobile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
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

        supportFragmentManager.setFragmentResultListener(
            ServerUrlDialog.REQUEST_KEY, this
        ) { _, bundle ->
            val url = bundle.getString(ServerUrlDialog.RESULT_URL) ?: return@setFragmentResultListener
            serverUrl = url
            saveServerUrl(url)
            webView.loadUrl(url)
        }

        binding.fabUpdate.setOnClickListener {
            UpdateManager(this).checkAndInstall()
        }
        binding.fabUpdate.setOnLongClickListener {
            promptForUrl(prefill = serverUrl)
            true
        }

        serverUrl = savedInstanceState?.getString(KEY_SERVER_URL)
            ?: loadServerUrl()

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
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        webView.setInitialScale(1)

        webView.webViewClient = TmuxWebViewClient { errorMessage ->
            runOnUiThread {
                Toast.makeText(
                    this,
                    getString(R.string.error_load_failed, errorMessage),
                    Toast.LENGTH_LONG
                ).show()
                promptForUrl()
            }
        }

        webView.webChromeClient = TmuxWebChromeClient(this)

        webView.addJavascriptInterface(TmuxBridge(this), "TmuxMobileAndroid")
    }

    private fun saveServerUrl(url: String) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putString(PREF_SERVER_URL, url)
            .apply()
    }

    private fun loadServerUrl(): String? {
        return PreferenceManager.getDefaultSharedPreferences(this)
            .getString(PREF_SERVER_URL, null)
    }

    private var isPromptPending = false

    private fun promptForUrl(prefill: String? = null) {
        if (supportFragmentManager.isStateSaved) {
            isPromptPending = true
            return
        }
        if (supportFragmentManager.findFragmentByTag(TAG_SERVER_URL) != null) return
        ServerUrlDialog.newInstance(prefill).show(supportFragmentManager, TAG_SERVER_URL)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (isPromptPending) {
            isPromptPending = false
            promptForUrl()
        }
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
        private const val TAG_SERVER_URL = "server_url"
        private const val PREF_SERVER_URL = "server_url"
    }
}
