package com.yurii.foody.screens.admin.products.editor

data class AdditionalImageData(val id: Long, val uriOrUrl: String, val type: String) {
    companion object {
        fun create(uriOrUrl: String, type: String): AdditionalImageData {
            val id = System.currentTimeMillis()/1000
            return AdditionalImageData(id, uriOrUrl, type)
        }
    }
}