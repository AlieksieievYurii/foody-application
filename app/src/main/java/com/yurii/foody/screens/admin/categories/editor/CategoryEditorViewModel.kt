package com.yurii.foody.screens.admin.categories.editor

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.yurii.foody.ui.UploadPhotoDialog
import com.yurii.foody.utils.Empty
import com.yurii.foody.utils.FieldValidation
import com.yurii.foody.utils.ProductsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class CategoryPhoto(val id: Long, val type: UploadPhotoDialog.Mode, val urlOrUri: String)

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

    fun resetProductNameFieldValidation() {}

    fun save() {}

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