package com.yurii.foody.screens.cook.execution

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentCookingStatusBinding
import com.yurii.foody.utils.toPx

@BindingAdapter("statusAttrChanged")
fun setListener(orderStatusComponent: OrderStatusComponent, listener: InverseBindingListener) {
    orderStatusComponent.setListener { listener.onChange() }
}

@BindingAdapter("status")
fun setValue(orderStatusComponent: OrderStatusComponent, status: OrderStatusComponent.Status) {
    if (orderStatusComponent.status != status)
        orderStatusComponent.status = status
}

@InverseBindingAdapter(attribute = "status")
fun getValue(orderStatusComponent: OrderStatusComponent): OrderStatusComponent.Status {
    return orderStatusComponent.status
}

class OrderStatusComponent(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    enum class Status {
        TAKING, COOKING, FINISHED, DELIVERED
    }

    private enum class Flag {
        DISABLE, ENABLE, DONE
    }

    private val binding: FragmentCookingStatusBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.fragment_cooking_status, this, true
    )

    var status: Status = Status.TAKING
        set(value) {
            field = value
            setStatusView()
        }

    init {
        setStatusView()
    }

    fun setListener(callback: (status: Status) -> Unit) {
        val listener: ((view: View) -> Unit) = { _ ->
            callback.invoke(status)
        }

        binding.apply {
            startCooking.setOnClickListener(listener)
            finish.setOnClickListener(listener)
            delivered.setOnClickListener(listener)
        }
    }

    fun observeLoading(isLoading: LiveData<Boolean>, lifecycleOwner: LifecycleOwner) {
        isLoading.observe(lifecycleOwner) { loading ->
            binding.apply {
                startCooking.isEnabled = !loading
                finish.isEnabled = !loading
                delivered.isEnabled = !loading
            }
        }
    }

    private fun setStatusView() {
        when (status) {
            Status.TAKING -> binding.apply {
                setCookingStatus(Flag.ENABLE)
                setFinishStatus(Flag.DISABLE)
                setDeliverStatus(Flag.DISABLE)
            }
            Status.COOKING -> binding.apply {
                setCookingStatus(Flag.DONE)
                setFinishStatus(Flag.ENABLE)
                setDeliverStatus(Flag.DISABLE)
            }
            Status.FINISHED -> {
                setCookingStatus(Flag.DONE)
                setFinishStatus(Flag.DONE)
                setDeliverStatus(Flag.ENABLE)
            }
            Status.DELIVERED -> {
                setCookingStatus(Flag.DONE)
                setDeliverStatus(Flag.DONE)
                setFinishStatus(Flag.DONE)
            }
        }
    }

    private fun setCookingStatus(flag: Flag) = when (flag) {
        Flag.DISABLE -> binding.apply {
            statusCooking.setBackgroundResource(R.drawable.circle_gray)
            startCooking.isVisible = false
            labelCooking.isVisible = true
            labelCooking.setTextColor(ContextCompat.getColor(context, R.color.gray))
        }
        Flag.ENABLE -> binding.apply {
            statusCooking.setBackgroundResource(R.drawable.circle_yellow)
            startCooking.isVisible = true
            labelCooking.isVisible = false
            setLineStatus(lineOne, Flag.ENABLE)
        }
        Flag.DONE -> binding.apply {
            statusCooking.setBackgroundResource(R.drawable.circle_yellow)
            startCooking.isVisible = false
            labelCooking.isVisible = true
            labelCooking.setTextColor(ContextCompat.getColor(context, R.color.black))
            setLineStatus(lineOne, Flag.DONE)
        }
    }

    private fun setFinishStatus(flag: Flag) = when (flag) {
        Flag.DISABLE -> binding.apply {
            statusFinished.setBackgroundResource(R.drawable.circle_gray)
            finish.isVisible = false
            labelFinished.isVisible = true
            labelFinished.setTextColor(ContextCompat.getColor(context, R.color.gray))
            setLineStatus(lineTwo, Flag.DISABLE)
        }
        Flag.ENABLE -> binding.apply {
            statusFinished.setBackgroundResource(R.drawable.circle_yellow)
            finish.isVisible = true
            labelFinished.isVisible = false
            setLineStatus(lineTwo, Flag.ENABLE)
        }
        Flag.DONE -> binding.apply {
            statusFinished.setBackgroundResource(R.drawable.circle_yellow)
            finish.isVisible = false
            labelFinished.isVisible = true
            labelFinished.setTextColor(ContextCompat.getColor(context, R.color.black))
            setLineStatus(lineTwo, Flag.DONE)
        }
    }

    private fun setDeliverStatus(flag: Flag) = when (flag) {
        Flag.DISABLE -> binding.apply {
            statusDelivered.setBackgroundResource(R.drawable.circle_gray)
            delivered.isVisible = false
            labelDelivered.isVisible = true
            labelDelivered.setTextColor(ContextCompat.getColor(context, R.color.gray))
        }
        Flag.ENABLE -> binding.apply {
            statusDelivered.setBackgroundResource(R.drawable.circle_yellow)
            delivered.isVisible = true
            labelDelivered.isVisible = false
            setLineStatus(lineThree, Flag.ENABLE)
        }
        Flag.DONE -> binding.apply {
            statusDelivered.setBackgroundResource(R.drawable.circle_yellow)
            delivered.isVisible = false
            labelDelivered.isVisible = true
            labelDelivered.setTextColor(ContextCompat.getColor(context, R.color.black))
            setLineStatus(lineOne, Flag.DONE)
            setLineStatus(lineTwo, Flag.DONE)
            setLineStatus(lineThree, Flag.DONE)
        }
    }

    private fun setLineStatus(line: View, flag: Flag) = when (flag) {
        Flag.DISABLE -> {
            line.layoutParams.height = 40.toPx
            line.setBackgroundResource(R.color.gray)
        }
        Flag.ENABLE -> {
            line.layoutParams.height = 80.toPx
            line.setBackgroundResource(R.color.yellow)
        }
        Flag.DONE -> {
            line.layoutParams.height = 40.toPx
            line.setBackgroundResource(R.color.yellow)
        }
    }

}