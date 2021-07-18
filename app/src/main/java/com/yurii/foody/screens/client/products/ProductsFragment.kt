package com.yurii.foody.screens.client.products

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentClientProductsBinding
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.closeFragment
import com.yurii.foody.utils.observeOnLifecycle
import com.yurii.foody.utils.setOnQueryTextListener

class ProductsFragment : Fragment(R.layout.fragment_client_products) {
    private val binding: FragmentClientProductsBinding by viewBinding()
    private val viewModel: ProductsViewModel by viewModels { Injector.provideProductsViewModel() }
    private val listAdapter: ProductAdapter by lazy { ProductAdapter(viewModel::onProductClick) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                ProductsViewModel.Event.Refresh -> listAdapter.refresh()
                is ProductsViewModel.Event.NavigateToProduct -> findNavController().navigate(
                    ProductsFragmentDirections.actionProductsFragmentToProductDetailFragment(
                        event.productItem.id
                    )
                )
            }
        }
    }
}