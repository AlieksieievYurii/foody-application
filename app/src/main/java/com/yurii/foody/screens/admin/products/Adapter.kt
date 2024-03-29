package com.yurii.foody.screens.admin.products

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
import com.yurii.foody.api.Product
import com.yurii.foody.api.ProductAvailability
import com.yurii.foody.api.Service
import com.yurii.foody.databinding.ItemProductEditBinding
import com.yurii.foody.utils.EmptyListException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class ProductData(
    val id: Long,
    val name: String,
    val price: Float,
    val available: Int,
    val isAvailable: Boolean,
    val isActive: Boolean,
    val rating: Float,
    val thumbnailUrl: String
) {
    companion object {
        fun createFrom(product: Product, productAvailability: ProductAvailability?, rating: Float?, thumbnailUrl: String?) = ProductData(
            id = product.id,
            name = product.name,
            price = product.price,
            isAvailable = productAvailability?.isAvailable ?: false,
            isActive = productAvailability?.isActive ?: false,
            rating = rating ?: 0f,
            thumbnailUrl = thumbnailUrl ?: "",
            available = productAvailability?.available ?: 0
        )
    }
}

class ProductPagingSource(
    private val api: Service,
    private val query: Query? = null
) : PagingSource<Int, ProductData>() {
    data class Query(
        var search: String? = null,
        var isActive: Boolean? = null,
        var isAvailable: Boolean? = null
    )

    override fun getRefreshKey(state: PagingState<Int, ProductData>): Int {
        return 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProductData> {
        return try {
            val page = params.key ?: 1
            val products = api.productsService.getProducts(query?.search, query?.isAvailable, query?.isActive, page=page, size=params.loadSize)

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
                ProductData.createFrom(product, availability, rating?.rating, thumbnail?.imageUrl)
            }

            LoadResult.Page(result, prevKey = if (page == 1) null else page - 1, nextKey = if (products.next != null) page + 1 else null)
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }
}

interface ProductViewHolderCallback {
    fun onClick(product: ProductData)
    fun isSelected(product: ProductData): Boolean
}

class ProductAdapter(private val selectableMode: MutableStateFlow<Boolean>, private val scope: CoroutineScope) :
    PagingDataAdapter<ProductData, ProductAdapter.ProductViewHolder>(COMPARATOR), ProductViewHolderCallback {
    private val selectedProducts: MutableSet<ProductData> = mutableSetOf()
    var onClickItem: ((ProductData) -> Unit)? = null
    init {
        scope.launch {
            selectableMode.collectLatest {
                if (!it)
                    selectedProducts.clear()
            }
        }
    }
    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<ProductData>() {
            override fun areItemsTheSame(oldItem: ProductData, newItem: ProductData): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ProductData, newItem: ProductData): Boolean =
                oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        getItem(position)?.run { holder.bind(this) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder.create(parent, selectableMode, scope, this)
    }

    fun getSelectedItems() = selectedProducts.toList()

    override fun onClick(product: ProductData) {
        if (selectableMode.value)
            if (isSelected(product))
                selectedProducts.remove(product)
            else
                selectedProducts.add(product)
        else
            onClickItem?.invoke(product)
    }

    override fun isSelected(product: ProductData): Boolean = selectedProducts.contains(product)


    class ProductViewHolder(
        private val binding: ItemProductEditBinding,
        private val selectableMode: MutableStateFlow<Boolean>,
        private val scope: CoroutineScope,
        private val callback: ProductViewHolderCallback
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductData) {
            binding.product = product

            applySelectableEffect(product)

            binding.body.setOnClickListener {
                callback.onClick(product)

                if (selectableMode.value)
                    applySelectableEffect(product)
            }

            scope.launch {
                selectableMode.collectLatest {
                    if (it)
                        return@collectLatest

                    binding.body.setOnLongClickListener {
                        selectableMode.value = true
                        callback.onClick(product)
                        applySelectableEffect(product)
                        true
                    }
                    applySelectableEffect(product)
                }
            }
        }

        private fun applySelectableEffect(product: ProductData) {
            val color = when {
                callback.isSelected(product) -> R.color.gray
                product.isActive -> R.color.white
                else -> R.color.light_red
            }
            binding.body.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, color))
        }


        companion object {
            fun create(
                viewGroup: ViewGroup,
                selectableMode: MutableStateFlow<Boolean>,
                scope: CoroutineScope,
                callback: ProductViewHolderCallback
            ): ProductViewHolder {
                val binding: ItemProductEditBinding =
                    DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context), R.layout.item_product_edit, viewGroup, false)
                return ProductViewHolder(binding, selectableMode, scope, callback)
            }
        }
    }

}