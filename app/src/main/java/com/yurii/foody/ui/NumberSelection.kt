package com.yurii.foody.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentNumberSelectionBinding

class NumberSelection(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val binding: FragmentNumberSelectionBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.fragment_number_selection, this, true
    )

    var number: Int = DEFAULT_VALUE
        set(value) {
            field = value
            binding.number.text = value.toString()
        }

    init {
        binding.number.text = number.toString()
        binding.increase.setOnClickListener {
            binding.number.text = (++number).toString()
        }
        binding.decrease.setOnClickListener {
            if (number > MINIMUM_VALUE)
                binding.number.text = (--number).toString()
        }
    }


    companion object {
        private const val MINIMUM_VALUE = 0
        private const val DEFAULT_VALUE = 0
    }
}