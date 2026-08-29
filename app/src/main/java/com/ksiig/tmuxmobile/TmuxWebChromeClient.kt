package com.ksiig.tmuxmobile

import android.webkit.JsPromptResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.fragment.app.FragmentActivity

/**
 * Bridges `window.prompt(message, defaultValue)` calls from the embedded
 * tmux-mobile frontend to a native [JsPromptDialog]. Android WebView silently
 * returns `null` from `window.prompt` when no `WebChromeClient` is installed,
 * which made the "+ New Session" button in tmux-mobile's drawer a no-op.
 *
 * Only [onJsPrompt] is overridden. `alert` and `confirm` keep the base
 * class's default behaviour so WebView's internal fallback still handles
 * them; tmux-mobile doesn't call either today.
 */
class TmuxWebChromeClient(
    private val activity: FragmentActivity
) : WebChromeClient() {

    override fun onJsPrompt(
        view: WebView,
        url: String?,
        message: String,
        defaultValue: String?,
        result: JsPromptResult
    ): Boolean {
        // If the activity is in a state where we can't show a dialog (already
        // destroyed, finishing, or FragmentManager has saved state), unblock
        // the JS thread by cancelling the prompt. Showing a dialog in those
        // states throws `IllegalStateException` from the FragmentManager.
        if (activity.isFinishing || activity.isDestroyed || activity.supportFragmentManager.isStateSaved) {
            result.cancel()
            return true
        }

        val requestKey = "${JsPromptDialog.REQUEST_KEY_PREFIX}:${System.identityHashCode(result)}"

        activity.supportFragmentManager.setFragmentResultListener(
            requestKey, activity
        ) { _, bundle ->
            if (bundle.containsKey(JsPromptDialog.RESULT_CANCELED)) {
                result.cancel()
            } else {
                result.confirm(bundle.getString(JsPromptDialog.RESULT_VALUE).orEmpty())
            }
        }

        JsPromptDialog.newInstance(requestKey, message, defaultValue)
            .show(activity.supportFragmentManager, requestKey)

        return true
    }
}
