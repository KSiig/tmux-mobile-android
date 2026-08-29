package com.ksiig.tmuxmobile

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * DialogFragment that mirrors [ServerUrlDialog]'s shape but is generic enough to
 * host any `window.prompt(message, defaultValue)` call from the embedded web
 * frontend. The result is delivered via the FragmentResult API under a
 * per-prompt request key so concurrent prompts can't collide.
 */
class JsPromptDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = arguments?.getString(ARG_MESSAGE).orEmpty()
        val defaultValue = arguments?.getString(ARG_DEFAULT_VALUE).orEmpty()
        val requestKey = requireArguments().getString(ARG_REQUEST_KEY)
            ?: error("$ARG_REQUEST_KEY is required")

        val layout = TextInputLayout(requireContext()).apply {
            setPadding(48, 16, 48, 0)
            hint = message
        }
        val input = TextInputEditText(layout.context).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            setText(defaultValue)
            setSelection(defaultValue.length)
        }
        layout.addView(input)

        return AlertDialog.Builder(requireContext())
            .setTitle(message)
            .setView(layout)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                setFragmentResult(
                    requestKey,
                    bundleOf(RESULT_VALUE to (input.text?.toString() ?: ""))
                )
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                setFragmentResult(requestKey, bundleOf(RESULT_CANCELED to true))
            }
            .create()
            .apply {
                setOnCancelListener {
                    setFragmentResult(requestKey, bundleOf(RESULT_CANCELED to true))
                }
                setCanceledOnTouchOutside(true)
            }
    }

    companion object {
        const val REQUEST_KEY_PREFIX = "js_prompt_request"
        const val RESULT_VALUE = "value"
        const val RESULT_CANCELED = "canceled"

        private const val ARG_REQUEST_KEY = "request_key"
        private const val ARG_MESSAGE = "message"
        private const val ARG_DEFAULT_VALUE = "default_value"

        fun newInstance(requestKey: String, message: String, defaultValue: String?) =
            JsPromptDialog().apply {
                arguments = bundleOf(
                    ARG_REQUEST_KEY to requestKey,
                    ARG_MESSAGE to message,
                    ARG_DEFAULT_VALUE to (defaultValue ?: "")
                )
            }
    }
}
