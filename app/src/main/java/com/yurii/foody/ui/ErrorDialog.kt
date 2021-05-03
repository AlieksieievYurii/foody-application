package com.yurii.foody.ui

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.yurii.foody.R
import com.yurii.foody.databinding.DialogErrorBinding

class ErrorDialog(context: Context) {
    private val binding: DialogErrorBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_error, null, false)
    }

    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context)
            .setView(binding.root)
            .create()
    }

    fun show(message: String) {
        if (dialog.isShowing)
            return
        dialog.show()
        binding.apply {
            error.text = message
            ok.setOnClickListener { close() }
        }
    }

    private fun close() {
        if (dialog.isShowing)
            dialog.dismiss()
    }
}