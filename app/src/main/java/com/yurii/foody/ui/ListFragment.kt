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
    sealed class State {
        object Loading : State()
        object Empty : State()
        object Ready : State()
        data class Error(val error: Throwable) : State()
    }

    private val binding: FragmentListBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.fragment_list, this, true
    )

    var isRefreshEnable: Boolean = true
        set(value) {
            field = value
            binding.refresh.isEnabled = value
        }

    var state: State = State.Loading
        set(value) {
            field = value
            binding.apply {
                when (value) {
                    State.Loading -> {
                        loading.isVisible = true
                        list.isVisible = false
                        emptyList.isVisible = false
                        refresh.isEnabled = isRefreshEnable
                        retry.isVisible = false
                        error.isVisible = false
                    }
                    State.Empty -> {
                        refresh.isEnabled = isRefreshEnable
                        refresh.isRefreshing = false
                        emptyList.isVisible = true
                        loading.isVisible = false
                        list.isVisible = false
                        retry.isVisible = false
                        error.isVisible = false
                    }
                    State.Ready -> {
                        emptyList.isVisible = false
                        loading.isVisible = false
                        list.isVisible = true
                        refresh.isEnabled = isRefreshEnable
                        refresh.isRefreshing = false
                        retry.isVisible = false
                        error.isVisible = false
                    }
                    is State.Error -> {
                        loading.isVisible = false
                        list.isVisible = false
                        emptyList.isVisible = false
                        refresh.isEnabled = isRefreshEnable
                        refresh.isRefreshing = false
                        retry.isVisible = true
                        error.isVisible = true
                        error.text = value.error.message
                    }
                }
            }
        }

    fun <T : Any, HV : RecyclerView.ViewHolder> setAdapter(adapter: PagingDataAdapter<T, HV>) {
        binding.list.adapter = adapter.withLoadStateFooter(LoaderStateAdapter())
        binding.list.layoutAnimation = android.view.animation.AnimationUtils.loadLayoutAnimation(context, R.anim.list_animation)
    }

    fun setOnRefreshListener(callback: () -> Unit) {
        binding.refresh.isEnabled = true
        binding.refresh.setOnRefreshListener {
            callback.invoke()
        }
    }

    fun setOnRetryListener(callback: () -> Unit) {
        binding.retry.setOnClickListener {
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