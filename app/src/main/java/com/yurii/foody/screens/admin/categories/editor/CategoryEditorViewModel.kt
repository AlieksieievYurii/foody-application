package com.yurii.foody.screens.admin.categories.editor

import android.app.Application
import androidx.core.net.toUri
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.yurii.foody.api.Category
import com.yurii.foody.ui.UploadPhotoDialog
import com.yurii.foody.utils.Empty
import com.yurii.foody.utils.FieldValidation
import com.yurii.foody.utils.ProductsRepository
import com.yurii.foody.utils.value
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

data class CategoryPhoto(val type: UploadPhotoDialog.Mode, val urlOrUri: String) {
    companion object {
        fun create(image: UploadPhotoDialog.Result) = when (image) {
            is UploadPhotoDialog.Result.External -> CategoryPhoto(UploadPhotoDialog.Mode.EXTERNAL, image.url)
            is UploadPhotoDialog.Result.Internal -> CategoryPhoto(UploadPhotoDialog.Mode.INTERNAL, image.uri.toString())
        }

        fun create(category: Category): CategoryPhoto = CategoryPhoto(
            if (category.isIconExternal) UploadPhotoDialog.Mode.EXTERNAL else UploadPhotoDialog.Mode.INTERNAL,
            urlOrUri = category.iconUrl
        )
    }
}

class CategoryEditorViewModel(
    application: Application,
    private val productsRepository: ProductsRepository,
    private val categoryIdToEdit: Long? = null
) : AndroidViewModel(application) {
    sealed class Event {
        data class ShowError(val exception: Throwable) : Event()
        object CloseEditor : Event()
    }

    val isEditMode = categoryIdToEdit != null

    private val _categoryPhoto: MutableStateFlow<CategoryPhoto?> = MutableStateFlow(null)
    val categoryPhoto: StateFlow<CategoryPhoto?> = _categoryPhoto

    val categoryPhotoFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val categoryNameFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val categoryName = ObservableField(String.Empty)

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    private var originalPhoto: CategoryPhoto? = null

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _isLoading.value = false
        viewModelScope.launch {
            eventChannel.send(Event.ShowError(exception))
        }
    }

    private val viewModelJob = SupervisorJob()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + coroutineExceptionHandler)

    init {
        if (isEditMode)
            loadCategoryToEdit()
    }

    private fun loadCategoryToEdit() {
        netWorkScope.launch {
            val category = productsRepository.getCategory(categoryIdToEdit!!)
            categoryName.set(category.name)
            _categoryPhoto.value = CategoryPhoto.create(category).also { originalPhoto = it }
        }
    }

    fun save() {
        if (isValidated())
            if (isEditMode)
                saveChanges()
            else
                createCategory()
    }

    private fun saveChanges() {
        netWorkScope.launch {
            _isLoading.value = true
            val iconUrl =
                if (originalPhoto != _categoryPhoto.value && _categoryPhoto.value!!.type == UploadPhotoDialog.Mode.INTERNAL) getPhotoUrl()
                else _categoryPhoto.value!!.urlOrUri

            productsRepository.updateCategory(
                Category(
                    id = categoryIdToEdit!!,
                    name = categoryName.value,
                    iconUrl = iconUrl,
                    isIconExternal = _categoryPhoto.value!!.type == UploadPhotoDialog.Mode.EXTERNAL
                )
            )
            _isLoading.value = false
            eventChannel.send(Event.CloseEditor)
        }
    }

    private fun createCategory() {
        netWorkScope.launch {
            _isLoading.value = true
            productsRepository.createCategory(
                Category.create(
                    name = categoryName.value,
                    iconUrl = getPhotoUrl(),
                    isIconExternal = _categoryPhoto.value!!.type == UploadPhotoDialog.Mode.EXTERNAL
                )
            )

            _isLoading.value = false
            eventChannel.send(Event.CloseEditor)
        }

    }

    private suspend fun getPhotoUrl(): String = when (_categoryPhoto.value!!.type) {
        UploadPhotoDialog.Mode.EXTERNAL -> _categoryPhoto.value!!.urlOrUri
        UploadPhotoDialog.Mode.INTERNAL -> uploadPhoto()
    }

    private suspend fun uploadPhoto() = withContext(Dispatchers.IO) {
        with(getApplication<Application>().contentResolver.openInputStream(_categoryPhoto.value!!.urlOrUri.toUri())) {
            productsRepository.uploadImage(this!!.readBytes()).url
        }
    }

    private fun isValidated(): Boolean {
        var isValidated = true

        if (_categoryPhoto.value == null)
            categoryPhotoFieldValidation.value = FieldValidation.NoPhoto.apply { isValidated = false }

        if (categoryName.value.isNullOrBlank())
            categoryNameFieldValidation.value = FieldValidation.EmptyField.apply { isValidated = false }

        return isValidated
    }

    fun setPhoto(photo: CategoryPhoto) {
        categoryPhotoFieldValidation.value = FieldValidation.NoErrors
        _categoryPhoto.value = photo
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
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