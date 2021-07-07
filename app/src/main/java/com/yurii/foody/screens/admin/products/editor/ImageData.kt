package com.yurii.foody.screens.admin.products.editor

import com.yurii.foody.ui.UploadPhotoDialog

data class ProductPhoto(val id: Long, val type: UploadPhotoDialog.Mode, val urlOrUri: String) {
    companion object {
        fun create(data: UploadPhotoDialog.Result): ProductPhoto {
            return when (data) {
                is UploadPhotoDialog.Result.External -> create(UploadPhotoDialog.Mode.EXTERNAL, data.url)
                is UploadPhotoDialog.Result.Internal -> create(UploadPhotoDialog.Mode.INTERNAL, data.uri.toString())
            }

        }

        fun create(type: UploadPhotoDialog.Mode, urlOrUri: String): ProductPhoto {
            val id = System.currentTimeMillis() / 1000
            return ProductPhoto(id, type, urlOrUri)
        }
    }
}

//fun List<ProductImage>.toAdditionalImageData(): List<AdditionalImageData> {
//    return this.map {
//        AdditionalImageData()
//    }
//}