package com.yurii.foody.screens.client.products


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.paging.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yurii.foody.R
import com.yurii.foody.api.Product
import com.yurii.foody.api.ProductAvailability
import com.yurii.foody.api.Service
import com.yurii.foody.databinding.ItemProductBinding
import com.yurii.foody.utils.EmptyListException
import com.yurii.foody.utils.observeOnLifecycle
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import java.io.IOException

data class ProductItem(
    val id: Long,
    val name: String,
    val price: Float,
    val rating: Float,
    val cookingTime: Int,
    val thumbnailUrl: String,
    val isAvailable: Boolean
) {
    val averageTime: String = "20-40"

    companion object {
        fun createFrom(product: Product, productAvailability: ProductAvailability?, rating: Float?, thumbnailUrl: String?) = ProductItem(
            id = product.id,
            name = product.name,
            price = product.price,
            cookingTime = product.cookingTime,
            isAvailable = productAvailability?.isAvailable ?: false,
            rating = rating ?: 0f,
            thumbnailUrl = thumbnailUrl ?: "",
        )
    }
}

class ProductsPagingSource(private val api: Service) : PagingSource<Int, ProductItem>() {
    override fun getRefreshKey(state: PagingState<Int, ProductItem>) = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProductItem> {
        return try {
            val page = params.key ?: 1
            val products = api.productsService.getProducts(page = page, size = params.loadSize)
            if (products.results.isEmpty())
                return LoadResult.Error(EmptyListException())

            val productIds = products.results.joinToString(",") { it.id.toString() }
            val productAvailability = api.productAvailability.getProductAvailability(productIds = productIds, size = params.loadSize)
            val productsImages = api.productImage.getProductsImages(size = params.loadSize, productIds = productIds, isDefault = true)
            val ratings = api.productsRatings.getProductsRatings(productIds)

            val result = products.results.map { product ->
                val availability = productAvailability.results.find { it.productId == product.id }
                val rating = ratings.find { it.productId == product.id }
                val thumbnail = productsImages.results.find { it.productId == product.id }
                ProductItem.createFrom(product, availability, rating?.rating, thumbnail?.imageUrl)
            }

            LoadResult.Page(result, prevKey = if (page == 1) null else page - 1, nextKey = if (products.next != null) page + 1 else null)
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }
}

class ProductAdapter(private val onClick: (product: ProductItem) -> Unit) :
    PagingDataAdapter<ProductItem, ProductAdapter.ProductItemViewHolder>(COMPARATOR) {
    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<ProductItem>() {
            override fun areItemsTheSame(oldItem: ProductItem, newItem: ProductItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ProductItem, newItem: ProductItem): Boolean =
                oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: ProductItemViewHolder, position: Int) {
        getItem(position)?.run {
            holder.bind(product = this, onClick = {
                onClick.invoke(this)
            })
        }
    }

    fun observeData(data: StateFlow<PagingData<ProductItem>>, lifecycleOwner: LifecycleOwner) {
        data.observeOnLifecycle(lifecycleOwner) {
            submitData(it)
        }
    }

    fun bindListState(callback: (state: CombinedLoadStates) -> Unit, lifecycleOwner: LifecycleOwner) {
        loadStateFlow.observeOnLifecycle(lifecycleOwner) { loadState ->
            callback.invoke(loadState)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductItemViewHolder {
        return ProductItemViewHolder.create(parent)
    }

    class ProductItemViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: ProductItem, onClick: (view: View) -> Unit) {
            binding.product = product
            binding.root.setOnClickListener(onClick)
        }

        companion object {
            fun create(viewGroup: ViewGroup): ProductItemViewHolder {
                val binding: ItemProductBinding =
                    DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context), R.layout.item_product, viewGroup, false)
                return ProductItemViewHolder(binding)
            }
        }
    }
}