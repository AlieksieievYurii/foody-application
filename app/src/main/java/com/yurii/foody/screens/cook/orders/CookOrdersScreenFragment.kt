package com.yurii.foody.screens.cook.orders

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentCookOrdersBinding
import com.yurii.foody.screens.cook.main.CookMainScreenViewModel
import com.yurii.foody.utils.Injector

class CookOrdersScreenFragment : Fragment(R.layout.fragment_cook_orders) {
    private val binding: FragmentCookOrdersBinding by viewBinding()
    private val viewModel: CookMainScreenViewModel by viewModels { Injector.provideCookOrdersViewModel(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
}