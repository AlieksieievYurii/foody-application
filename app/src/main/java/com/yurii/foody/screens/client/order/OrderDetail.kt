package com.yurii.foody.screens.client.order

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentOrderDetailBinding
import com.yurii.foody.utils.Injector

class OrderDetail : Fragment(R.layout.fragment_order_detail) {
    private val binding: FragmentOrderDetailBinding by viewBinding()
    private val args: OrderDetailArgs by navArgs()
    private val viewModel: OrderDetailViewModel by viewModels { Injector.provideOrderDetailViewModel(requireContext(), orderId = args.orderId) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }
}