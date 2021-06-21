package com.yurii.foody.ui

import android.content.Context
import android.net.Uri
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import coil.load
import com.yurii.foody.R
import com.yurii.foody.databinding.DialogPhotoUploadBinding
import java.lang.IllegalStateException

class UploadPhotoDialog(private val context: Context, registry: ActivityResultRegistry) {
    private enum class Mode { EXTERNAL, INTERNAL }
    private enum class Error { SELECT_IMAGE, ENTER_IMAGE_URL }
    sealed class Result {
        data class External(val url: String) : Result()
        data class Internal(val uri: Uri) : Result()
    }

    private var mode: Mode = Mode.EXTERNAL
    private var imageUri: Uri? = null
    private val binding: DialogPhotoUploadBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_photo_upload, null, false)
    }

    private val uploadingPhotoIntent = registry.register("Image", ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        binding.apply {
            preview.isVisible = true
            preview.load(uri)
            error.isVisible = false
        }
    }

    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context)
            .setView(binding.root)
            .setTitle(R.string.label_photo_uploading)
            .setPositiveButton(R.string.label_add, null)
            .setNegativeButton(R.string.label_cancel) { _, _ -> }
            .create()
    }

    init {
        binding.photoType.setOnCheckedChangeListener { _, id ->
            mode = when (id) {
                R.id.external -> {
                    showExternalUploadingOption()
                    Mode.EXTERNAL
                }
                R.id.internal -> {
                    showInternalUploadOption()
                    Mode.INTERNAL
                }
                else -> throw IllegalStateException("Unhandled mode")
            }
        }
    }

    private fun showExternalUploadingOption() {
        binding.apply {
            imageUrlLayout.isVisible = true
            select.isVisible = false
            error.isVisible = false
            preview.isVisible = false
            imageUrl.setOnKeyListener { _, keyCode, keyEvent ->
                if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    binding.preview.apply {
                        isVisible = true
                        error.isVisible = false
                        load(binding.imageUrl.text.toString()) {
                            error(R.drawable.image_error_placeholder)
                        }
                    }
                    return@setOnKeyListener true
                }
                false
            }
        }
    }

    private fun showInternalUploadOption() {
        binding.apply {
            preview.isVisible = false
            imageUrlLayout.isVisible = false
            error.isVisible = false
            select.isVisible = true
            select.setOnClickListener { uploadingPhotoIntent.launch("image/*") }
        }
    }

    private fun showError(error: Error) {
        binding.error.apply {
            isVisible = true
            text = context.getText(
                when (error) {
                    Error.SELECT_IMAGE -> R.string.error_select_image
                    Error.ENTER_IMAGE_URL -> R.string.error_enter_image_url
                }
            )
        }
    }

    fun show(callback: (result: Result) -> Unit) {
        imageUri = null
        binding.preview.isVisible = false
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            when (mode) {
                Mode.EXTERNAL -> {
                    val imageUrl = binding.imageUrl.text.toString()
                    if (imageUrl.isNotEmpty()) {
                        callback.invoke(Result.External(imageUrl))
                        dialog.cancel()
                        dialog.dismiss()
                    } else
                        showError(Error.ENTER_IMAGE_URL)
                }

                Mode.INTERNAL -> {
                    if (imageUri != null) {
                        callback.invoke(Result.Internal(imageUri!!))
                        dialog.dismiss()
                    } else
                        showError(Error.SELECT_IMAGE)
                }
            }
        }
    }
}