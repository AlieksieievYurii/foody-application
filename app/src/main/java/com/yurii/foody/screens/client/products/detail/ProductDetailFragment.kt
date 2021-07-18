package com.yurii.foody.screens.client.products.detail

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentProductDetailBinding
import com.yurii.foody.utils.Injector

class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {
    private val binding: FragmentProductDetailBinding by viewBinding()
    private val args: ProductDetailFragmentArgs by navArgs()
    private val viewModel: ProductDetailViewModel by viewModels { Injector.provideProductDetailViewModel(args.productId) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val images = mutableListOf<String>()
        images.add("https://www.pizzagirlpatrol.com/wp-content/uploads/2018/07/Optimized-2742A125-5C6D-47C4-A63D-48969B5D8EE9.jpg")
        images.add("https://www.pizzagirlpatrol.com/wp-content/uploads/2018/07/Optimized-2742A125-5C6D-47C4-A63D-48969B5D8EE9.jpg")
        images.add("https://www.pizzagirlpatrol.com/wp-content/uploads/2018/07/Optimized-2742A125-5C6D-47C4-A63D-48969B5D8EE9.jpg")
        images.add("https://www.pizzagirlpatrol.com/wp-content/uploads/2018/07/Optimized-2742A125-5C6D-47C4-A63D-48969B5D8EE9.jpg")
        images.add("https://www.pizzagirlpatrol.com/wp-content/uploads/2018/07/Optimized-2742A125-5C6D-47C4-A63D-48969B5D8EE9.jpg")

        binding.images.setImages(images)
    }
}