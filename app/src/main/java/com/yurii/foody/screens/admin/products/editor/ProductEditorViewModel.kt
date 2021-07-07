package com.yurii.foody.screens.admin.products.editor

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.yurii.foody.api.*
import com.yurii.foody.ui.UploadPhotoDialog
import com.yurii.foody.utils.CategoryRepository
import com.yurii.foody.utils.Empty
import com.yurii.foody.utils.FieldValidation
import com.yurii.foody.utils.ProductsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow

class ProductEditorViewModel(
    application: Application,
    private val categoryRepository: CategoryRepository,
    private val productsRepository: ProductsRepository,
    private val productIdToEdit: Long? = null
) : AndroidViewModel(application) {
    sealed class Event {
        data class ShowError(val exception: Throwable) : Event()
        object CloseEditor : Event()
    }

    private val _mainPhoto: MutableStateFlow<ProductPhoto?> = MutableStateFlow(null)
    val mainPhoto: StateFlow<ProductPhoto?> = _mainPhoto

    private val _additionalImagesFlow: MutableStateFlow<MutableList<ProductPhoto>> = MutableStateFlow(mutableListOf())
    val additionalImagesFlow: StateFlow<MutableList<ProductPhoto>> = _additionalImagesFlow

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

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val productName = ObservableField(String.Empty)
    val description = ObservableField(String.Empty)
    val price = ObservableField(String.Empty)
    val cookingTime = ObservableField(String.Empty)
    var category: CategoryItem? = null
    var availability = ObservableField(0)
    var isAvailable = ObservableField(false)
    var isActive = ObservableField(false)

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()


    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _isLoading.value = false
        viewModelScope.launch {
            eventChannel.send(Event.ShowError(exception))
        }
    }

    init {
        viewModelScope.launch {
            categoryRepository.getCategories().collectLatest {
                _categories.value = it
            }
        }

        if (productIdToEdit != null)
            loadProductToEdit()
    }

    private fun loadProductToEdit() {
        viewModelScope.launch {
            _isLoading.value = true
            loadProduct()
            loadAvailability()
            loadMainImage()
            loadAdditionalImages()
            _isLoading.value = false
        }
    }

    private suspend fun loadMainImage() {
        _mainPhoto.value = ProductPhoto.create(productsRepository.getMainProductImage(productIdToEdit!!))
    }

    private suspend fun loadAdditionalImages() {
        _additionalImagesFlow.value =
            productsRepository.getAdditionalProductImages(productIdToEdit!!)
                .toAdditionalImageData()
                .toMutableList()
    }

    private suspend fun loadAvailability() {
        productsRepository.getProductAvailability(productIdToEdit!!).also { productAvailability ->
            availability.set(productAvailability.available)
            isAvailable.set(productAvailability.isAvailable)
            isActive.set(productAvailability.isActive)
        }
    }

    private suspend fun loadProduct() {
        productsRepository.getProduct(productIdToEdit!!).also { product ->
            productName.set(product.name)
            description.set(product.description)
            price.set(product.price.toString())
            cookingTime.set(product.cookingTime.toString())
        }
    }

    fun save() {
        if (areFieldsValidated())
            createNewProduct()
    }

    private fun createNewProduct() {
        viewModelScope.launch(coroutineExceptionHandler) {
            _isLoading.value = true
            val product = createProduct()
            awaitAll(
                async { createProductAvailability(product) },
                async {
                    if (isCategorySelected())
                        createProductCategory(product)
                },
                async { loadDefaultProductImage(product) },
                async { loadAdditionalPhotos(product) }
            )
            _isLoading.value = false
            eventChannel.send(Event.CloseEditor)
        }
    }

    private suspend fun loadAdditionalPhotos(product: Product) {
        additionalImagesFlow.value.forEach {
            loadPhoto(product, it, isDefault = false)
        }
    }

    private suspend fun loadDefaultProductImage(product: Product): ProductImage {
        return loadPhoto(product, _mainPhoto.value!!, isDefault = true)
    }

    private suspend fun loadPhoto(product: Product, photo: ProductPhoto, isDefault: Boolean = false): ProductImage {
        return when (photo.type) {
            UploadPhotoDialog.Mode.EXTERNAL -> loadExternalPhoto(product, photo.urlOrUri, isDefault)
            UploadPhotoDialog.Mode.INTERNAL -> loadInternalPhoto(product, photo.urlOrUri.toUri(), isDefault)
        }
    }

    private suspend fun loadInternalPhoto(product: Product, uri: Uri, isDefault: Boolean): ProductImage {
        val loadedPhotoUrl = withContext(Dispatchers.IO) {
            with(getApplication<Application>().contentResolver.openInputStream(uri)) {
                productsRepository.uploadImage(this!!.readBytes())
            }
        }

        return productsRepository.createProductImage(
            ProductImage.create(
                imageUrl = loadedPhotoUrl.url,
                isDefault = isDefault,
                isExternal = false,
                productId = product.id
            )
        )
    }


    private suspend fun loadExternalPhoto(product: Product, url: String, isDefault: Boolean) =
        productsRepository.createProductImage(
            productImage = ProductImage.create(
                imageUrl = url,
                isDefault = isDefault,
                isExternal = true,
                productId = product.id
            )
        )

    private suspend fun createProductCategory(product: Product) = productsRepository.createProductCategory(
        productCategory = ProductCategory(
            product = product.id,
            category = category!!.id
        )
    )

    private fun isCategorySelected(): Boolean = category?.id != -1L

    private suspend fun createProductAvailability(product: Product) = productsRepository.createProductAvailability(
        productAvailability = ProductAvailability.create(
            available = availability.get()!!,
            isAvailable = isAvailable.get()!!,
            isActive = isActive.get()!!,
            productId = product.id
        )
    )

    private suspend fun createProduct() = productsRepository.createProduct(
        product = Product.create(
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

    fun addMainPhoto(photo: ProductPhoto) {
        _mainPhoto.value = photo
        _defaultPhotoFieldValidation.value = FieldValidation.NoErrors
    }

    fun addAdditionalImage(photo: ProductPhoto) {
        _additionalImagesFlow.value = mutableListOf<ProductPhoto>().apply {
            addAll(_additionalImagesFlow.value)
            add(photo)
        }
    }

    fun removeAdditionalImage(photo: ProductPhoto) {
        _additionalImagesFlow.value = mutableListOf<ProductPhoto>().apply {
            addAll(_additionalImagesFlow.value)
            remove(photo)
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

    class Factory(
        private val application: Application,
        private val categoryRepository: CategoryRepository,
        private val productsRepository: ProductsRepository,
        private val productIdToEdit: Long?
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductEditorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductEditorViewModel(application, categoryRepository, productsRepository, productIdToEdit) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}