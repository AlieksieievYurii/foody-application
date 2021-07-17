package com.yurii.foody.screens.client.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentClientProductsBinding
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.closeFragment
import com.yurii.foody.utils.observeOnLifecycle
import com.yurii.foody.utils.setOnQueryTextListener
import timber.log.Timber

class ProductsFragment : Fragment() {
    private lateinit var binding: FragmentClientProductsBinding
    private val viewModel: ProductsViewModel by viewModels { Injector.provideProductsViewModel() }
    private val listAdapter: ProductAdapter by lazy { ProductAdapter(viewModel::onProductClick) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_client_products, container, false)
        binding.back.setOnClickListener { closeFragment() }
        binding.searchBar.setOnQueryTextListener(viewModel::searchProduct)

        binding.listFragment.apply {
            setAdapter(listAdapter)
            observeListState(viewModel.listState)
            setOnRefreshListener(viewModel::refreshList)
            setOnRetryListener(listAdapter::retry)
        }

        listAdapter.apply {
            observeData(viewModel.products, viewLifecycleOwner)
            bindListState(viewModel::onLoadStateChange, viewLifecycleOwner)
        }

        observeEvents()
        return binding.root
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                ProductsViewModel.Event.Refresh -> listAdapter.refresh()
                is ProductsViewModel.Event.NavigateToProduct -> Timber.i(event.productItem.name)
            }
        }
    }
}