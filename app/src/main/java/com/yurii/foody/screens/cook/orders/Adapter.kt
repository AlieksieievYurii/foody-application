package com.yurii.foody.screens.cook.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.paging.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yurii.foody.R
import com.yurii.foody.api.*
import com.yurii.foody.databinding.ItemOrderBinding
import com.yurii.foody.utils.*
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import java.io.IOException

data class Order(
    val id: Long,
    val thumbnail: String,
    val client: Long,
    val product: Product,
    val timestamp: Long,
    val cookingTime: Int,
    val price: Float,
    val count: Int
) {
    val total = count * price
    val averageTime = convertToAverageTime(cookingTime)
    val timestampDateTime: String = toSimpleDateTime(timestamp)
    val isDelayed: Boolean = isOrderDelayed(timestamp, cookingTime)
}

class OrdersPagingSource(private val api: Service) : PagingSource<Int, Order>() {
    override fun getRefreshKey(state: PagingState<Int, Order>): Int {
        return 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Order> {
        return try {
            val page = params.key ?: 1
            val orders = api.orders.getOrders(page = page, size = params.loadSize, ordering = "timestamp", isTaken = false)
            if (orders.results.isEmpty())
                return LoadResult.Error(EmptyListException())
            val productIds = orders.results.joinToString(",") { it.product.toString() }
            val products = api.productsService.getProducts(ids = productIds, size = params.loadSize)
            val productDefaultImages = api.productImage.getProductsImages(productIds = productIds, size = params.loadSize, isDefault = true)
            val result = orders.results.map { order ->
                val product = products.results.find { it.id == order.product }!!
                Order(
                    id = order.id,
                    thumbnail = productDefaultImages.results.find { it.productId == order.product }!!.imageUrl,
                    client = order.user,
                    product = product,
                    timestamp = toTimestampInSeconds(order.timestamp),
                    cookingTime = product.cookingTime,
                    price = product.price,
                    count = order.count
                )
            }
            LoadResult.Page(
                result.sortedBy { !it.isDelayed },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (orders.next != null) page + 1 else null
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }
}

class OrdersAdapter(private val lifecycleOwner: LifecycleOwner, private val onClick: (Order) -> Unit) :
    PagingDataAdapter<Order, OrdersAdapter.OrderViewHolder>(COMPARATOR) {
    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean =
                oldItem == newItem
        }
    }

    fun observeOrders(ordersPagerStateFlow: StateFlow<PagingData<Order>>) {
        ordersPagerStateFlow.observeOnLifecycle(lifecycleOwner) {
            submitData(it)
        }
    }

    fun observeStateFlow(onStateChange: (CombinedLoadStates) -> Unit) {
        loadStateFlow.observeOnLifecycle(lifecycleOwner) { loadState ->
            onStateChange(loadState)
        }
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        getItem(position)?.run { holder.bind(this, onClick) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder.create(parent)
    }

    class OrderViewHolder private constructor(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order, onClick: (Order) -> Unit) {
            binding.order = order
            binding.body.setOnClickListener { onClick.invoke(order) }
        }

        companion object {
            fun create(viewGroup: ViewGroup): OrderViewHolder {
                val binding: ItemOrderBinding =
                    DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context), R.layout.item_order, viewGroup, false)
                return OrderViewHolder(binding)
            }
        }
    }
}