package com.yurii.foody.utils

import com.yurii.foody.api.Service

class CategoryRepository private constructor(private val service: Service) {

    fun getCategories() = Service.asFlow { service.categories.getCategories() }

    companion object {
        private var INSTANCE: CategoryRepository? = null
        fun create(api: Service): CategoryRepository {
            if (INSTANCE == null)
                synchronized(CategoryRepository::class.java) { INSTANCE = CategoryRepository(api) }

            return INSTANCE!!
        }
    }
}