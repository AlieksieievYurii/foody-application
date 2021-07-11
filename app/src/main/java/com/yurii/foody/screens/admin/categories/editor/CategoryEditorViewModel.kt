package com.yurii.foody.screens.admin.categories.editor

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.yurii.foody.ui.UploadPhotoDialog
import com.yurii.foody.utils.Empty
import com.yurii.foody.utils.FieldValidation
import com.yurii.foody.utils.ProductsRepository
import com.yurii.foody.utils.value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class CategoryPhoto(val type: UploadPhotoDialog.Mode, val urlOrUri: String) {
    companion object {
        fun create(image: UploadPhotoDialog.Result): CategoryPhoto {
            return when (image) {
                is UploadPhotoDialog.Result.External -> CategoryPhoto(UploadPhotoDialog.Mode.EXTERNAL, image.url)
                is UploadPhotoDialog.Result.Internal -> CategoryPhoto(UploadPhotoDialog.Mode.INTERNAL, image.uri.toString())
            }

        }
    }
}

class CategoryEditorViewModel(
    application: Application,
    private val productsRepository: ProductsRepository,
    private val categoryIdToEdit: Long? = null
) : AndroidViewModel(application) {

    private val _categoryPhoto: MutableStateFlow<CategoryPhoto?> = MutableStateFlow(null)
    val categoryPhoto: StateFlow<CategoryPhoto?> = _categoryPhoto

    private val _categoryPhotoFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val categoryPhotoFieldValidation: LiveData<FieldValidation> = _categoryPhotoFieldValidation

    private val _categoryNameFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val categoryNameFieldValidation: LiveData<FieldValidation> = _categoryNameFieldValidation

    val categoryName = ObservableField(String.Empty)

    fun resetProductNameFieldValidation() {
        _categoryNameFieldValidation.value = FieldValidation.NoErrors
    }

    fun save() {
        if (isValidated())
            createCategory()
    }

    private fun createCategory() {

    }

    private fun isValidated(): Boolean {
        var areErrors = false

        if (_categoryPhoto.value == null)
            _categoryPhotoFieldValidation.value = FieldValidation.NoPhoto.apply { areErrors = true }

        if (categoryName.value.isNullOrEmpty())
            _categoryNameFieldValidation.value = FieldValidation.EmptyField.apply { areErrors = true }

        return areErrors
    }

    fun setPhoto(photo: CategoryPhoto) {
        _categoryPhotoFieldValidation.value = FieldValidation.NoErrors
        _categoryPhoto.value = photo
    }

    class Factory(
        private val application: Application,
        private val productsRepository: ProductsRepository,
        private val categoryIdToEdit: Long?
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CategoryEditorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CategoryEditorViewModel(application, productsRepository, categoryIdToEdit) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}