package com.yurii.foody.screens.admin.products.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yurii.foody.ui.UploadPhotoDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProductEditorViewModel : ViewModel() {
    val mainPhoto: MutableStateFlow<UploadPhotoDialog.Result?> = MutableStateFlow(null)

    private val _additionalImagesFlow: MutableStateFlow<MutableList<AdditionalImageData>> = MutableStateFlow(mutableListOf())
    val additionalImagesFlow: StateFlow<MutableList<AdditionalImageData>> = _additionalImagesFlow

    fun addAdditionalImage(imageData: AdditionalImageData) {
        _additionalImagesFlow.value = mutableListOf<AdditionalImageData>().apply {
            addAll(_additionalImagesFlow.value)
            add(imageData)
        }
    }

    fun removeAdditionalImage(imageData: AdditionalImageData) {
        _additionalImagesFlow.value = mutableListOf<AdditionalImageData>().apply {
            addAll(_additionalImagesFlow.value)
            remove(imageData)
        }
    }

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductEditorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductEditorViewModel() as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}