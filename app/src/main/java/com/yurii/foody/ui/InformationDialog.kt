package com.yurii.foody.ui

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.yurii.foody.R
import com.yurii.foody.databinding.DialogInformationBinding

class InformationDialog(context: Context, private val isCancelable: Boolean = true, private val gotItCallBack: (() -> Unit)? = null) {
    private val binding: DialogInformationBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_information, null, false)
    }
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(isCancelable)
            .create()
    }

    fun show(message: String) {
        if (dialog.isShowing)
            return
        dialog.show()
        binding.apply {
            this.message.text = message
            gotIt.setOnClickListener {
                gotItCallBack?.invoke()
                close()
            }
        }
    }

    private fun close() {
        if (dialog.isShowing)
            dialog.dismiss()
    }
}