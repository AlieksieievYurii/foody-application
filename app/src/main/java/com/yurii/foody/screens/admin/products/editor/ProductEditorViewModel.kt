package com.yurii.foody.screens.admin.products.editor

import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.yurii.foody.api.Category
import com.yurii.foody.api.Product
import com.yurii.foody.api.ProductAvailability
import com.yurii.foody.ui.UploadPhotoDialog
import com.yurii.foody.utils.CategoryRepository
import com.yurii.foody.utils.Empty
import com.yurii.foody.utils.FieldValidation
import com.yurii.foody.utils.ProductsRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ProductEditorViewModel(private val categoryRepository: CategoryRepository, private val productsRepository: ProductsRepository) : ViewModel() {
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

    private val _categories: MutableStateFlow<List<Category>> = MutableStateFlow(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    val productName = ObservableField(String.Empty)
    val description = ObservableField(String.Empty)
    val price = ObservableField(String.Empty)
    val cookingTime = ObservableField(String.Empty)
    var category: CategoryItem? = null
    var availability: Int = 0
    var isAvailable = ObservableField(false)
    var isActive = ObservableField(false)


    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        when (exception) {
            is HttpException -> {
            }
            else -> {
            }
        }
    }

    init {
        viewModelScope.launch {
            categoryRepository.getCategories().collectLatest {
                _categories.value = it
            }
        }
    }

    fun save() {
        if (areFieldsValidated())
            createNewProduct()
    }

    private fun createNewProduct() {
        viewModelScope.launch(coroutineExceptionHandler) {
            val product = createProduct()
            createProductAvailability(product)
        }
    }

    private suspend fun createProductAvailability(product: Product) = productsRepository.createProductAvailability(
        productAvailability = ProductAvailability(
            id = -1, // No needed for creating
            available = availability,
            isAvailable = isAvailable.get()!!,
            isActive = isActive.get()!!,
            productId = product.id
        )
    )

    private suspend fun createProduct() = productsRepository.createProduct(
        product = Product(
            id = -1, // No needed
            name = productName.get()!!,
            description = description.get()!!,
            price = price.get()!!.toFloat(),
            cookingTime = cookingTime.get()!!.toInt()
        )
    )

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

    class Factory(private val categoryRepository: CategoryRepository, private val productsRepository: ProductsRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductEditorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductEditorViewModel(categoryRepository, productsRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}