package com.yurii.foody.screens.admin.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yurii.foody.utils.ProductsRepository
import timber.log.Timber

class CategoriesEditorViewModel(private val productsRepository: ProductsRepository) : ViewModel() {

    fun test() {
        Timber.i("TEST")
    }

    class Factory(private val repository: ProductsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CategoriesEditorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CategoriesEditorViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}