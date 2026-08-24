package com.ksiig.tmuxmobile

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ServerUrlDialog(
    private val onConnect: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layout = TextInputLayout(requireContext()).apply {
            setPadding(48, 16, 48, 0)
            hint = getString(R.string.server_url_hint)
        }
        val input = TextInputEditText(layout.context).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_URI
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
                onConnect(normalized)
            }
            .setCancelable(false)
            .create()
    }
}
