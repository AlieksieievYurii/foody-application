package com.yurii.foody.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentListBinding

class ListFragment(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val binding: FragmentListBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.fragment_list, this, true
    )

    private var isSwipeListenerSet: Boolean = false

    var isLoading: Boolean = false
        set(value) {
            field = value
            binding.loading.isVisible = value
            binding.list.isVisible = !value
            binding.refresh.isEnabled = !value && isSwipeListenerSet
        }

    var isListEmpty: Boolean = false
        set(value) {
            field = value
            binding.emptyList.isVisible = value
            binding.list.isVisible = !value
        }

    var isUpdating: Boolean = false
        set(value) {
            field = value
            binding.refresh.isRefreshing = isUpdating
        }

    fun <T : Any, HV : RecyclerView.ViewHolder> setAdapter(adapter: PagingDataAdapter<T, HV>) {
        binding.list.adapter = adapter.withLoadStateFooter(LoaderStateAdapter())
    }

    fun setOnRefreshListener(callback: () -> Unit) {
        isSwipeListenerSet = true
        binding.refresh.isEnabled = true
        binding.refresh.setOnRefreshListener {
            callback.invoke()
        }
    }
}

class LoaderStateAdapter : LoadStateAdapter<LoaderStateAdapter.LoaderViewHolder>() {
    class LoaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun create(viewGroup: ViewGroup): LoaderViewHolder {
                val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_loading, viewGroup, false)
                return LoaderViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: LoaderViewHolder, loadState: LoadState) {
        //nothing
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoaderViewHolder {
        return LoaderViewHolder.create(parent)
    }
}