package com.yurii.foody.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentNumberSelectionBinding

@BindingAdapter("valueAttrChanged")
fun setListener(numberSelection: NumberSelection, listener: InverseBindingListener) {
    numberSelection.onChangeListener = {
        listener.onChange()
    }
}

@BindingAdapter("value")
fun setValue(numberSelection: NumberSelection, value: Int) {
    if (numberSelection.number != value)
        numberSelection.number = value
}

@BindingAdapter("maxValue")
fun setMaxValue(numberSelection: NumberSelection, value: Int) {
    numberSelection.maxNumber = value
}

@InverseBindingAdapter(attribute = "value")
fun getValue(numberSelection: NumberSelection): Int {
    return numberSelection.number
}

class NumberSelection(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val binding: FragmentNumberSelectionBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.fragment_number_selection, this, true
    )

    var onChangeListener: ((n: Int) -> Unit)? = null

    var number: Int = DEFAULT_VALUE
        set(value) {
            field = value
            binding.number.text = value.toString()
        }
    var maxNumber: Int = MAXIMUM_VALUE

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.NumberSelection, 0, 0).apply {
            number = getInteger(R.styleable.NumberSelection_value, DEFAULT_VALUE)
            maxNumber = getInteger(R.styleable.NumberSelection_maxValue, MAXIMUM_VALUE)
        }

        binding.number.text = number.toString()
        binding.increase.setOnClickListener {
            if (number < maxNumber) {
                binding.number.text = (++number).toString()
                onChangeListener?.invoke(number)
            }
        }
        binding.decrease.setOnClickListener {
            if (number > MINIMUM_VALUE) {
                binding.number.text = (--number).toString()
                onChangeListener?.invoke(number)
            }
        }
    }

    companion object {
        private const val MINIMUM_VALUE = 0
        private const val DEFAULT_VALUE = 0
        private const val MAXIMUM_VALUE = 100
    }
}