package com.yurii.foody.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentImageSliderBinding

@BindingAdapter("images")
fun setValue(imageSlide: ImageSlider, images: List<String>?) {
    images?.run { imageSlide.setImages(images) }
}

class ImageSlider(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private class ImageSliderAdapter(private val images: List<String>) : RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {
        class ImageViewHolder(private val view: ImageView) : RecyclerView.ViewHolder(view) {
            fun bind(url: String) {
                view.load(url)
            }

            companion object {
                fun create(viewGroup: ViewGroup): ImageViewHolder {
                    val imageView = ImageView(viewGroup.context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    }

                    return ImageViewHolder(imageView)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ImageViewHolder.create(parent)
        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) = holder.bind(images[position])
        override fun getItemCount() = images.size
    }

    fun setImages(images: List<String>) {
        binding.images.adapter = ImageSliderAdapter(images)
    }


    private val binding: FragmentImageSliderBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.fragment_image_slider, this, true
    )
}