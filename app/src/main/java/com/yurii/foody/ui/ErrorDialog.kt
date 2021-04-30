package com.yurii.foody.ui

import android.content.Context
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.yurii.foody.R

class ErrorDialog(context: Context) {
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context)
            .setView(R.layout.dialog_error)
            .create()
    }

    fun show(message: String) {
        if (dialog.isShowing)
            return
        dialog.show()
        dialog.findViewById<TextView>(R.id.error)?.text = message
        dialog.findViewById<Button>(R.id.ok)?.setOnClickListener { close() }
    }

    private fun close() {
        if (dialog.isShowing)
            dialog.dismiss()
    }
}