package com.yurii.foody.screens.client.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.paging.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yurii.foody.R
import com.yurii.foody.api.*
import com.yurii.foody.databinding.ItemHistoryOrderBinding
import com.yurii.foody.databinding.ItemPendingOrderBinding
import com.yurii.foody.utils.EmptyListException
import com.yurii.foody.utils.observeOnLifecycle
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import java.io.IOException
import java.lang.IllegalStateException

sealed class Item(val id: Long) {
    data class PendingItem(
        val idItem: Long,
        val title: String
    ) : Item(idItem) {
        companion object {
            fun createFrom(order: Order): PendingItem =
                PendingItem(
                    idItem = order.id,
                    title = order.product.toString()
                )
        }
    }

    data class HistoryItem(
        val idItem: Long,
        val product: Product?,
        val productImage: String,
        val count: Int,
        val price: Float
    ) : Item(idItem) {
        val total = price * count

        companion object {
            fun createFrom(history: History, product: Product?, productImage: String) =
                HistoryItem(
                    idItem = history.id,
                    product = product,
                    count = history.count,
                    productImage = productImage,
                    price = history.price
                )
        }
    }
}

class HistoryPagingSource(private val api: Service) : PagingSource<Int, Item>() {
    override fun getRefreshKey(state: PagingState<Int, Item>) = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Item> {
        return try {
            val page = params.key ?: 1
            val results: MutableList<Item> = if (page == 1) getPendingItems(params.loadSize).toMutableList() else mutableListOf()

            val myHistory = api.ordersExecution.getUserHistory(mine = true)
            val productsIds = myHistory.results.map { it.product }.joinToString(",")
            val products = api.productsService.getProducts(ids = productsIds).results
            val productsImages = api.productImage.getProductsImages(productIds = productsIds, isDefault = true).results
            results.addAll(myHistory.results.map {
                Item.HistoryItem.createFrom(
                    it, product = products.find { product -> product.id == it.product },
                    productImage = productsImages.find { productImage -> productImage.productId == it.product }?.imageUrl ?: ""
                )
            })

            if (results.isEmpty())
                return LoadResult.Error(EmptyListException())

            LoadResult.Page(results, prevKey = if (page == 1) null else page - 1, nextKey = if (myHistory.next != null) page + 1 else null)
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }

    }

    private suspend fun getPendingItems(loadSize: Int, page: Int = 1): MutableList<Item.PendingItem> {
        val myOrders = api.orders.getOrders(size = loadSize, mine = true, page = page)
        val results = myOrders.results.map { Item.PendingItem.createFrom(it) }.toMutableList()

        if (myOrders.next != null)
            results.addAll(getPendingItems(loadSize, page + 1))

        return results
    }
}

class HistoryAndPendingItemsAdapter(private val lifecycleOwner: LifecycleOwner, private val onClick: (item: Item) -> Unit) :
    PagingDataAdapter<Item, HistoryAndPendingItemsAdapter.ItemViewHolder>(COMPARATOR) {
    companion object {
        private const val HISTORY_ITEM = 1
        private const val PENDING_ITEM = 2
        private val COMPARATOR = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
                oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) = holder.bind(getItem(position), onClick)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        HISTORY_ITEM -> ItemViewHolder.HistoryItemViewHolder.create(parent)
        PENDING_ITEM -> ItemViewHolder.PendingItemViewHolder.create(parent)
        else -> throw IllegalStateException()
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is Item.HistoryItem -> HISTORY_ITEM
        is Item.PendingItem -> PENDING_ITEM
        null -> throw IllegalStateException()
    }

    fun observeData(data: StateFlow<PagingData<Item>>) {
        data.observeOnLifecycle(lifecycleOwner) {
            submitData(it)
        }
    }

    fun bindListState(callback: (state: CombinedLoadStates) -> Unit) {
        loadStateFlow.observeOnLifecycle(lifecycleOwner) { loadState ->
            callback.invoke(loadState)
        }
    }

    sealed class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: Item?, onClick: (item: Item) -> Unit)

        class PendingItemViewHolder private constructor(private val binding: ItemPendingOrderBinding) : ItemViewHolder(binding.root) {
            companion object {
                fun create(viewGroup: ViewGroup): PendingItemViewHolder {
                    val binding: ItemPendingOrderBinding =
                        DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context), R.layout.item_pending_order, viewGroup, false)
                    return PendingItemViewHolder(binding)
                }
            }

            override fun bind(item: Item?, onClick: (item: Item) -> Unit) {
                item?.run {
                    binding.pendingItem = this as Item.PendingItem
                    binding.body.setOnClickListener { onClick.invoke(item) }
                }
            }
        }

        class HistoryItemViewHolder private constructor(private val binding: ItemHistoryOrderBinding) : ItemViewHolder(binding.root) {
            companion object {
                fun create(viewGroup: ViewGroup): HistoryItemViewHolder {
                    val binding: ItemHistoryOrderBinding =
                        DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context), R.layout.item_history_order, viewGroup, false)
                    return HistoryItemViewHolder(binding)
                }
            }

            override fun bind(item: Item?, onClick: (item: Item) -> Unit) {
                item?.run {
                    binding.historyItem = this as Item.HistoryItem
                    binding.body.setOnClickListener { onClick.invoke(item) }
                }
            }
        }

    }
}