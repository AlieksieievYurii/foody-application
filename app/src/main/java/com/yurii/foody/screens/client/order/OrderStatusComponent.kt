package com.yurii.foody.screens.client.order

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentOrderStatusBinding
import com.yurii.foody.utils.toPx

@BindingAdapter("status")
fun setValue(orderStatusComponent: OrderStatusComponent, status: OrderStatusComponent.Status) {
    if (orderStatusComponent.status != status)
        orderStatusComponent.status = status
}

class OrderStatusComponent(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    enum class Status {
        PENDING, COOKING, FINISHED, DELIVERED
    }

    private enum class Flag {
        DISABLE, ENABLE, DONE
    }

    private val binding: FragmentOrderStatusBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.fragment_order_status, this, true
    )

    var status: Status = Status.PENDING
        set(value) {
            field = value
            setStatusView()
        }

    init {
        setStatusView()
    }

    private fun setStatusView() {
        when (status) {
            Status.PENDING -> {
                setPendingStatus(Flag.ENABLE)
                setCookingStatus(Flag.DISABLE)
                setFinishStatus(Flag.DISABLE)
                setDeliveredStatus(Flag.DISABLE)
            }
            Status.COOKING -> {
                setPendingStatus(Flag.DONE)
                setCookingStatus(Flag.ENABLE)
                setFinishStatus(Flag.DISABLE)
                setDeliveredStatus(Flag.DISABLE)
            }
            Status.FINISHED -> {
                setPendingStatus(Flag.DONE)
                setCookingStatus(Flag.DONE)
                setFinishStatus(Flag.ENABLE)
                setDeliveredStatus(Flag.DISABLE)
            }
            Status.DELIVERED -> {
                setPendingStatus(Flag.DONE)
                setCookingStatus(Flag.DONE)
                setFinishStatus(Flag.DONE)
                setDeliveredStatus(Flag.ENABLE)
            }
        }
    }

    private fun setPendingStatus(flag: Flag) {
        when (flag) {
            Flag.DISABLE -> binding.apply {
                setLineStatus(lineOne, Flag.DISABLE)
                pendingStatus.setBackgroundResource(R.drawable.circle_gray)
            }
            Flag.ENABLE -> binding.apply {
                setLineStatus(lineOne, Flag.ENABLE)
                pendingStatus.setBackgroundResource(R.drawable.circle_yellow)
            }
            Flag.DONE -> binding.apply {
                setLineStatus(lineOne, Flag.DONE)
                pendingStatus.setBackgroundResource(R.drawable.circle_yellow)
            }
        }
    }

    private fun setCookingStatus(flag: Flag) {
        when (flag) {
            Flag.DISABLE -> binding.apply {
                cookingStatus.setBackgroundResource(R.drawable.circle_gray)
                setLineStatus(lineTwo, Flag.DISABLE)
            }
            Flag.ENABLE -> binding.apply {
                cookingStatus.setBackgroundResource(R.drawable.circle_yellow)
                setLineStatus(lineTwo, Flag.ENABLE)
            }
            Flag.DONE -> binding.apply {
                cookingStatus.setBackgroundResource(R.drawable.circle_yellow)
                setLineStatus(lineTwo, Flag.DONE)
            }
        }
    }

    private fun setFinishStatus(flag: Flag) {
        when (flag) {
            Flag.DISABLE -> binding.apply {
                finishStatus.setBackgroundResource(R.drawable.circle_gray)
                setLineStatus(lineThree, Flag.DISABLE)
            }
            Flag.ENABLE -> binding.apply {
                finishStatus.setBackgroundResource(R.drawable.circle_yellow)
                setLineStatus(lineThree, Flag.ENABLE)
            }
            Flag.DONE -> binding.apply {
                finishStatus.setBackgroundResource(R.drawable.circle_yellow)
                setLineStatus(lineThree, Flag.DONE)
            }
        }
    }

    private fun setDeliveredStatus(flag: Flag) {
        when (flag) {
            Flag.DISABLE -> binding.apply {
                deliveredStatus.setBackgroundResource(R.drawable.circle_gray)
            }
            Flag.ENABLE -> binding.apply {
                deliveredStatus.setBackgroundResource(R.drawable.circle_yellow)

            }
            Flag.DONE -> binding.apply {
                deliveredStatus.setBackgroundResource(R.drawable.circle_yellow)
            }
        }
    }

    private fun setLineStatus(line: View, flag: Flag) = when (flag) {
        Flag.DISABLE -> {
            line.layoutParams.width = 40.toPx
            line.setBackgroundResource(R.color.gray)
        }
        Flag.ENABLE -> {
            line.layoutParams.width = 80.toPx
            line.setBackgroundResource(R.color.yellow)
        }
        Flag.DONE -> {
            line.layoutParams.width = 40.toPx
            line.setBackgroundResource(R.color.yellow)
        }
    }

}