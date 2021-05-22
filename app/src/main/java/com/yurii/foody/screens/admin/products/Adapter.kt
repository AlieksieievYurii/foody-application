package com.yurii.foody.screens.admin.products

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yurii.foody.R
import com.yurii.foody.api.ApiProducts
import com.yurii.foody.api.Product
import com.yurii.foody.databinding.TestItemBinding
import com.yurii.foody.utils.EmptyListException
import retrofit2.HttpException
import java.io.IOException


class ProductPagingSource(
    private val apiProducts: ApiProducts,
    private val query: Query? = null
) : PagingSource<Int, Product>() {
    data class Query(
        var search: String? = null,
        var isActive: Boolean? = null,
        var isAvailable: Boolean? = null
    )

    override fun getRefreshKey(state: PagingState<Int, Product>): Int {
        return 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
        return try {
            val page = params.key ?: 1
            val response = apiProducts.getProducts(query?.search, query?.isAvailable, query?.isActive, page, params.loadSize)

            if (response.results.isEmpty())
                return LoadResult.Error(EmptyListException())

            LoadResult.Page(
                response.results, prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.next != null) page + 1 else null
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }
}

class ProductAdapter : PagingDataAdapter<Product, ProductAdapter.ProductViewHolder>(COMPARATOR) {
    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean =
                oldItem == newItem
        }
    }

    class ProductViewHolder(private val binding: TestItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.title.text = product.name
        }

        companion object {
            fun create(viewGroup: ViewGroup): ProductViewHolder {
                val binding: TestItemBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context), R.layout.test_item, viewGroup, false)
                return ProductViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder.create(parent)
    }
}