package com.yurii.foody.screens.admin.products.editor

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.yurii.foody.api.*
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
import retrofit2.HttpException

class ProductEditorViewModel(
    application: Application,
    private val productsRepository: ProductsRepository,
    private val productIdToEdit: Long? = null
) : AndroidViewModel(application) {
    sealed class Event {
        data class ShowError(val exception: Throwable) : Event()
        object CloseEditor : Event()
    }
    val isEditMode = productIdToEdit != null

    private val _mainPhoto: MutableStateFlow<ProductPhoto?> = MutableStateFlow(null)
    val mainPhoto: StateFlow<ProductPhoto?> = _mainPhoto

    private val _additionalImagesFlow: MutableStateFlow<MutableList<ProductPhoto>> = MutableStateFlow(mutableListOf())
    val additionalImagesFlow: StateFlow<MutableList<ProductPhoto>> = _additionalImagesFlow

    private val _defaultPhotoFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val defaultPhotoFieldValidation: LiveData<FieldValidation> = _defaultPhotoFieldValidation

    val productNameFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val productDescriptionFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val priceFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)

     val cookingTimeFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)

    private val _categories: MutableStateFlow<List<Category>> = MutableStateFlow(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val productName = ObservableField(String.Empty)
    val description = ObservableField(String.Empty)
    val price = ObservableField(String.Empty)
    val cookingTime = ObservableField(String.Empty)
    val category = ObservableField(CategoryItem.NoCategory)
    var availability = ObservableField(0)
    var isAvailable = ObservableField(false)
    var isActive = ObservableField(false)

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    private var originalCategory: CategoryItem = CategoryItem.NoCategory
    private var originalMainPhoto: ProductPhoto? = null
    private var originalAdditionalPhotos: List<ProductPhoto>? = null

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _isLoading.value = false
        viewModelScope.launch {
            eventChannel.send(Event.ShowError(exception))
        }
    }

    private val viewModelJob = Job()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + coroutineExceptionHandler)

    init {
        if (isEditMode)
            loadProductToEdit()
        else
            loadDataForCreatingProduct()
    }

    private fun loadDataForCreatingProduct() {
        netWorkScope.launch {
            _categories.value = productsRepository.getCategories()
        }
    }

    private fun loadProductToEdit() {
        netWorkScope.launch {
            _isLoading.value = true
            awaitAll(
                async { loadProduct() },
                async { loadAvailability() },
                async { loadCategory() },
                async { loadMainImage() },
                async { loadAdditionalImages() }
            )
            _isLoading.value = false
        }
    }

    private suspend fun loadCategory() {
        _categories.value = productsRepository.getCategories()
        try {
            val categoryId = productsRepository.getProductCategory(productIdToEdit!!).category
            (_categories.value.find { it.id == categoryId }?.toCategoryItem() ?: CategoryItem.NoCategory).run {
                category.set(this)
                originalCategory = this
            }
        } catch (exception: HttpException) {
            category.set(CategoryItem.NoCategory)
        }
    }

    private suspend fun loadMainImage() {
        _mainPhoto.value = ProductPhoto.create(productsRepository.getMainProductImage(productIdToEdit!!)).also {
            originalMainPhoto = it
        }
    }

    private suspend fun loadAdditionalImages() {
        _additionalImagesFlow.value = productsRepository.getAdditionalProductImages(productIdToEdit!!)
            .toAdditionalImageData().also { originalAdditionalPhotos = it }
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
            if (isEditMode)
                saveChanges()
            else
                createNewProduct()
    }

    private fun saveChanges() {
        netWorkScope.launch {
            _isLoading.value = true
            updateProductInformation()
            updateProductAvailability()
            updateCategory()
            updateMainPhoto()
            updateAdditionalPhotos()
            _isLoading.value = false
            eventChannel.send(Event.CloseEditor)
        }

    }

    private suspend fun updateAdditionalPhotos() {
        val setOfOriginPhotos = originalAdditionalPhotos!!.toSet()
        val setOfCurrentPhotos = _additionalImagesFlow.value.toSet()
        val newPhotos = setOfCurrentPhotos - setOfOriginPhotos
        val deleted = originalAdditionalPhotos!!.toSet() - _additionalImagesFlow.value.toSet()

        deleteOldPhotos(deleted.toList())
        addNewPhotos(newPhotos.toList())
    }

    private suspend fun addNewPhotos(newPhotos: List<ProductPhoto>) {
        for (newPhoto in newPhotos)
            loadPhoto(productIdToEdit!!, newPhoto, isDefault = false)
    }

    private suspend fun deleteOldPhotos(oldPhotos: List<ProductPhoto>) {
        for (oldPhoto in oldPhotos)
            productsRepository.deleteProductImage(oldPhoto.id)
    }

    private suspend fun updateMainPhoto() {
        if (originalMainPhoto != _mainPhoto.value) {
            productsRepository.deleteProductImage(originalMainPhoto!!.id)
            loadPhoto(productIdToEdit!!, _mainPhoto.value!!, isDefault = true)
        }
    }

    private suspend fun updateCategory() {
        if (category.value == CategoryItem.NoCategory && originalCategory != CategoryItem.NoCategory)
            productsRepository.removeProductCategory(productIdToEdit!!)
        else
            updateExistedProductCategory()

    }

    private suspend fun updateExistedProductCategory() {
        if (category.value == CategoryItem.NoCategory)
            return

        val productCategory = ProductCategory(
            product = productIdToEdit!!,
            category = category.value.id
        )
        if (originalCategory == CategoryItem.NoCategory)
            productsRepository.createProductCategory(productCategory)
        else
            productsRepository.updateProductCategory(productIdToEdit, productCategory)
    }

    private suspend fun updateProductAvailability() {
        productsRepository.updateProductAvailability(
            ProductAvailability.create(
                available = availability.value,
                isAvailable = isAvailable.value,
                isActive = isActive.value,
                productId = productIdToEdit!!
            )
        )
    }

    private suspend fun updateProductInformation() {
        productsRepository.updateProduct(
            Product(
                id = productIdToEdit!!,
                name = productName.value,
                description = description.value,
                price = price.value.toFloat(),
                cookingTime = cookingTime.value.toInt()
            )
        )
    }

    private fun createNewProduct() {
        netWorkScope.launch {
            _isLoading.value = true
            val product = createProduct()
            awaitAll(
                async { createProductAvailability(product) },
                async {
                    if (isCategorySelected())
                        createProductCategory(product)
                },
                async { createDefaultProductImage(product) },
                async { createAdditionalPhotos(product) }
            )
            _isLoading.value = false
            eventChannel.send(Event.CloseEditor)
        }
    }

    private suspend fun createAdditionalPhotos(product: Product) {
        additionalImagesFlow.value.forEach {
            loadPhoto(product.id, it, isDefault = false)
        }
    }

    private suspend fun createDefaultProductImage(product: Product): ProductImage {
        return loadPhoto(product.id, _mainPhoto.value!!, isDefault = true)
    }

    private suspend fun loadPhoto(productId: Long, photo: ProductPhoto, isDefault: Boolean = false): ProductImage {
        return when (photo.type) {
            UploadPhotoDialog.Mode.EXTERNAL -> loadExternalPhoto(productId, photo.urlOrUri, isDefault)
            UploadPhotoDialog.Mode.INTERNAL -> loadInternalPhoto(productId, photo.urlOrUri.toUri(), isDefault)
        }
    }

    private suspend fun loadInternalPhoto(productId: Long, uri: Uri, isDefault: Boolean): ProductImage {
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
                productId = productId
            )
        )
    }


    private suspend fun loadExternalPhoto(productId: Long, url: String, isDefault: Boolean) =
        productsRepository.createProductImage(
            productImage = ProductImage.create(
                imageUrl = url,
                isDefault = isDefault,
                isExternal = true,
                productId = productId
            )
        )

    private suspend fun createProductCategory(product: Product) = productsRepository.createProductCategory(
        productCategory = ProductCategory(
            product = product.id,
            category = category.value.id
        )
    )

    private fun isCategorySelected(): Boolean = category.value != CategoryItem.NoCategory

    private suspend fun createProductAvailability(product: Product) = productsRepository.createProductAvailability(
        productAvailability = ProductAvailability.create(
            available = availability.value,
            isAvailable = isAvailable.value,
            isActive = isActive.value,
            productId = product.id
        )
    )

    private suspend fun createProduct() = productsRepository.createProduct(
        product = Product.create(
            name = productName.value,
            description = description.value,
            price = price.value.toFloat(),
            cookingTime = cookingTime.value.toInt()
        )
    )

    private fun areFieldsValidated(): Boolean {
        var isValid = true

        if (productName.get().isNullOrBlank())
            productNameFieldValidation.value = FieldValidation.EmptyField.also { isValid = false }

        if (price.get().isNullOrBlank())
            priceFieldValidation.value = FieldValidation.EmptyField.also { isValid = false }

        if (cookingTime.get().isNullOrBlank())
            cookingTimeFieldValidation.value = FieldValidation.EmptyField.also { isValid = false }

        if (_mainPhoto.value == null)
            _defaultPhotoFieldValidation.value = FieldValidation.NoPhoto.also { isValid = false }

        if (description.get().isNullOrBlank())
            productDescriptionFieldValidation.value = FieldValidation.EmptyField.also { isValid = false }

        return isValid
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

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class Factory(
        private val application: Application,
        private val productsRepository: ProductsRepository,
        private val productIdToEdit: Long?
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductEditorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductEditorViewModel(application, productsRepository, productIdToEdit) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}