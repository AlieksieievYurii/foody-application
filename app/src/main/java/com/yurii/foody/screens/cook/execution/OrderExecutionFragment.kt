package com.yurii.foody.screens.cook.execution

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentOrderExecutionBinding
import com.yurii.foody.utils.Injector

class OrderExecutionFragment : Fragment(R.layout.fragment_order_execution) {
    private val binding: FragmentOrderExecutionBinding by viewBinding()
    private val viewModel: OrderExecutionViewModel by viewModels { Injector.provideOrderExecutionViewModel(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
}