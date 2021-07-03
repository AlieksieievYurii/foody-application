package com.yurii.foody.screens.admin.products.editor

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yurii.foody.ui.UploadPhotoDialog
import com.yurii.foody.utils.Empty
import com.yurii.foody.utils.FieldValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class ProductEditorViewModel : ViewModel() {
    private val _mainPhoto: MutableStateFlow<UploadPhotoDialog.Result?> = MutableStateFlow(null)
    val mainPhoto: StateFlow<UploadPhotoDialog.Result?> = _mainPhoto

    private val _additionalImagesFlow: MutableStateFlow<MutableList<AdditionalImageData>> = MutableStateFlow(mutableListOf())
    val additionalImagesFlow: StateFlow<MutableList<AdditionalImageData>> = _additionalImagesFlow

    private val _defaultPhotoFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val defaultPhotoFieldValidation: LiveData<FieldValidation> = _defaultPhotoFieldValidation

    private val _productNameFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val productNameFieldValidation: LiveData<FieldValidation> = _productNameFieldValidation

    private val _priceFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val priceFieldValidation: LiveData<FieldValidation> = _priceFieldValidation

    private val _cookingTimeFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val cookingTimeFieldValidation: LiveData<FieldValidation> = _cookingTimeFieldValidation

    val productName = ObservableField(String.Empty)
    val description = ObservableField(String.Empty)
    val price = ObservableField(String.Empty)
    val cookingTime = ObservableField(String.Empty)

    fun save() {
        if (areFieldsValidated()) {

        }
    }

    private fun areFieldsValidated(): Boolean {
        var isValidated = true

        if (productName.get().isNullOrEmpty())
            _productNameFieldValidation.value = FieldValidation.EmptyField.also { isValidated = false }

        if (price.get().isNullOrEmpty())
            _priceFieldValidation.value = FieldValidation.EmptyField.also { isValidated = false }

        if (cookingTime.get().isNullOrEmpty())
            _cookingTimeFieldValidation.value = FieldValidation.EmptyField.also { isValidated = false }

        if (_mainPhoto.value == null)
            _defaultPhotoFieldValidation.value = FieldValidation.NoPhoto.also { isValidated = false }

        return isValidated
    }

    fun addMainPhoto(photo: UploadPhotoDialog.Result) {
        _mainPhoto.value = photo
        _defaultPhotoFieldValidation.value = FieldValidation.NoErrors
    }

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

    fun resetProductNameFieldValidation() {
        _productNameFieldValidation.value = FieldValidation.NoErrors
    }

    fun resetPriceFieldValidation() {
        _priceFieldValidation.value = FieldValidation.NoErrors
    }

    fun resetCookingTimeFieldValidation() {
        _cookingTimeFieldValidation.value = FieldValidation.NoErrors
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