package com.yurii.foody.screens.client.products.detail

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentProductDetailBinding
import com.yurii.foody.ui.ErrorDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.observeOnLifecycle

class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {
    private val binding: FragmentProductDetailBinding by viewBinding()
    private val args: ProductDetailFragmentArgs by navArgs()
    private val errorDialog by lazy { ErrorDialog(requireContext()) }
    private val viewModel: ProductDetailViewModel by viewModels { Injector.provideProductDetailViewModel(args.productId) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.images.observe(viewModel.images, viewLifecycleOwner)

        observeEvents()
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                is ProductDetailViewModel.Event.ShowError -> errorDialog.show(event.exception.message ?: getString(R.string.label_no_message))
            }
        }
    }
}