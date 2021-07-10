package com.yurii.foody.screens.admin.products.editor

import com.yurii.foody.api.Category
import com.yurii.foody.api.ProductImage
import com.yurii.foody.ui.UploadPhotoDialog

data class CategoryItem(val id: Long, val name: String) {
    override fun toString(): String {
        return name
    }

    companion object {
        val NoCategory = CategoryItem(-1, "---")
    }
}

fun Category.toCategoryItem(): CategoryItem {
    return CategoryItem(id = this.id, name = this.name)
}

data class ProductPhoto(val id: Long, val type: UploadPhotoDialog.Mode, val urlOrUri: String) {
    companion object {
        fun create(data: UploadPhotoDialog.Result): ProductPhoto {
            return when (data) {
                is UploadPhotoDialog.Result.External -> create(UploadPhotoDialog.Mode.EXTERNAL, data.url)
                is UploadPhotoDialog.Result.Internal -> create(UploadPhotoDialog.Mode.INTERNAL, data.uri.toString())
            }

        }

        fun create(productImage: ProductImage): ProductPhoto {
            return ProductPhoto(
                id = productImage.id,
                type = if (productImage.isExternal) UploadPhotoDialog.Mode.EXTERNAL else UploadPhotoDialog.Mode.INTERNAL,
                urlOrUri = productImage.imageUrl
            )
        }

        fun create(type: UploadPhotoDialog.Mode, urlOrUri: String): ProductPhoto {
            val id = System.currentTimeMillis() / 1000
            return ProductPhoto(id, type, urlOrUri)
        }
    }
}

fun List<ProductImage>.toAdditionalImageData(): List<ProductPhoto> {
    return this.map {
        ProductPhoto.create(it)
    }
}