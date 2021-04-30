package com.yurii.foody.ui

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.yurii.foody.R

class LoadingDialog(context: Context) {
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context)
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create().apply { window!!.setBackgroundDrawableResource(R.color.transparent) }
    }

    fun show() {
        if (!dialog.isShowing)
            dialog.show()
    }

    fun close() {
        if (dialog.isShowing)
            dialog.dismiss()
    }
}