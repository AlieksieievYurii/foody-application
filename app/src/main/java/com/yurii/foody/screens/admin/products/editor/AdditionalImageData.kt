package com.yurii.foody.screens.admin.products.editor

import com.yurii.foody.ui.UploadPhotoDialog

data class AdditionalImageData(val id: Long, val data: UploadPhotoDialog.Result) {
    companion object {
        fun create(data: UploadPhotoDialog.Result): AdditionalImageData {
            val id = System.currentTimeMillis()/1000
            return AdditionalImageData(id, data)
        }
    }
}