package com.yurii.foody.screens.admin.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yurii.foody.R
import com.yurii.foody.api.Category
import com.yurii.foody.api.Service
import com.yurii.foody.databinding.ItemCategoryBinding
import com.yurii.foody.utils.EmptyListException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class CategoriesData(
    val id: Long,
    val name: String,
    val thumbnailUrl: String
) {
    companion object {
        fun createFrom(category: Category): CategoriesData = CategoriesData(
            id = category.id,
            name = category.name,
            thumbnailUrl = category.iconUrl
        )
    }
}

class CategoriesPagingSource(private val api: Service) : PagingSource<Int, CategoriesData>() {
    override fun getRefreshKey(state: PagingState<Int, CategoriesData>): Int {
        return 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CategoriesData> {
        return try {
            val page = params.key ?: 1
            val categories = api.categories.getCategories(page, params.loadSize)

            if (categories.results.isEmpty())
                LoadResult.Error(EmptyListException())
            else
                LoadResult.Page(
                    categories.results.map { CategoriesData.createFrom(it) },
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (categories.next != null) page + 1 else null
                )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }
}

interface CategoryViewHolderCallback {
    fun onClick(product: CategoriesData)
    fun isSelected(product: CategoriesData): Boolean
}

class CategoriesAdapter(private val selectableMode: MutableStateFlow<Boolean>, private val scope: CoroutineScope) :
    PagingDataAdapter<CategoriesData, CategoriesAdapter.CategoriesViewHolder>(COMPARATOR), CategoryViewHolderCallback {
    private val selectedCategories: MutableSet<CategoriesData> = mutableSetOf()
    var onClickItem: ((CategoriesData) -> Unit)? = null

    init {
        scope.launch {
            selectableMode.collectLatest {
                if (!it)
                    selectedCategories.clear()
            }
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<CategoriesData>() {
            override fun areItemsTheSame(oldItem: CategoriesData, newItem: CategoriesData): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: CategoriesData, newItem: CategoriesData): Boolean =
                oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        getItem(position)?.run { holder.bind(this) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        return CategoriesViewHolder.create(parent, selectableMode, scope, this)
    }

    fun getSelectedItems() = selectedCategories.toList()

    override fun onClick(product: CategoriesData) {
        if (selectableMode.value)
            if (isSelected(product))
                selectedCategories.remove(product)
            else
                selectedCategories.add(product)
        else
            onClickItem?.invoke(product)
    }

    override fun isSelected(product: CategoriesData): Boolean = selectedCategories.contains(product)


    class CategoriesViewHolder(
        private val binding: ItemCategoryBinding,
        private val selectableMode: MutableStateFlow<Boolean>,
        private val scope: CoroutineScope,
        private val callback: CategoryViewHolderCallback
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoriesData) {
            binding.category = category

            applySelectableEffect(category)

            binding.body.setOnClickListener {
                callback.onClick(category)

                if (selectableMode.value)
                    applySelectableEffect(category)
            }

            scope.launch {
                selectableMode.collectLatest {
                    if (it)
                        return@collectLatest

                    binding.body.setOnLongClickListener {
                        selectableMode.value = true
                        callback.onClick(category)
                        applySelectableEffect(category)
                        true
                    }
                    applySelectableEffect(category)
                }
            }
        }

        private fun applySelectableEffect(category: CategoriesData) {
            val color = if (callback.isSelected(category)) R.color.gray else R.color.white
            binding.body.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, color))
        }


        companion object {
            fun create(
                viewGroup: ViewGroup,
                selectableMode: MutableStateFlow<Boolean>,
                scope: CoroutineScope,
                callback: CategoryViewHolderCallback
            ): CategoriesViewHolder {
                val binding: ItemCategoryBinding =
                    DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context), R.layout.item_category, viewGroup, false)
                return CategoriesViewHolder(binding, selectableMode, scope, callback)
            }
        }
    }

}