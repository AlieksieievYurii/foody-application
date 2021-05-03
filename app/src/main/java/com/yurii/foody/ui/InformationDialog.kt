package com.yurii.foody.ui

import android.content.Context
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.yurii.foody.R
import org.w3c.dom.Text

class InformationDialog(context: Context, private val isCancelable: Boolean = true, private val gotItCallBack: (() -> Unit)? = null) {
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context)
            .setView(R.layout.dialog_information)
            .setCancelable(isCancelable)
            .create()
    }

    fun show(message: String) {
        if (dialog.isShowing)
            return
        dialog.show()
        dialog.findViewById<TextView>(R.id.message)?.text = message
        dialog.findViewById<Button>(R.id.got_it)?.setOnClickListener {
            gotItCallBack?.invoke()
            close()
        }

    }

    private fun close() {
        if (dialog.isShowing)
            dialog.dismiss()
    }
}