package com.ksiig.tmuxmobile

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ServerUrlDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val currentUrl = arguments?.getString(ARG_CURRENT_URL)

        val layout = TextInputLayout(requireContext()).apply {
            setPadding(48, 16, 48, 0)
            hint = getString(R.string.server_url_hint)
        }
        val input = TextInputEditText(layout.context).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_URI
            currentUrl?.let { setText(it) }
        }
        layout.addView(input)

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.connect)
            .setView(layout)
            .setPositiveButton(R.string.connect) { _, _ ->
                val url = input.text?.toString()?.trim()
                if (url.isNullOrEmpty()) {
                    return@setPositiveButton
                }
                val normalized = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    "https://$url"
                } else {
                    url
                }
                setFragmentResult(REQUEST_KEY, bundleOf(RESULT_URL to normalized))
            }
            .setCancelable(arguments?.getString(ARG_CURRENT_URL) != null)
            .create()
    }

    companion object {
        const val REQUEST_KEY = "server_url_request"
        const val RESULT_URL = "url"
        private const val ARG_CURRENT_URL = "current_url"

        fun newInstance(currentUrl: String? = null) = ServerUrlDialog().apply {
            arguments = bundleOf(ARG_CURRENT_URL to currentUrl)
        }
    }
}
