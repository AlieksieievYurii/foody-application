package com.yurii.foody.screens.admin.products.editor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yurii.foody.R
import com.yurii.foody.databinding.ItemProductImageBinding
import com.yurii.foody.ui.UploadPhotoDialog
import com.yurii.foody.utils.loadImage
import java.lang.IllegalStateException

class ImagesListAdapter(private val onAddNewImage: () -> Unit, private val onItemDelete: (image: ProductPhoto) -> Unit) :
    ListAdapter<ProductPhoto, RecyclerView.ViewHolder>(COMPARATOR) {
    companion object {
        private const val ITEM_TYPE_IMAGE = 0
        private const val ITEM_TYPE_BUTTON_ADD = 1

        private val COMPARATOR = object : DiffUtil.ItemCallback<ProductPhoto>() {
            override fun areItemsTheSame(oldItem: ProductPhoto, newItem: ProductPhoto): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ProductPhoto, newItem: ProductPhoto): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_IMAGE -> ImageHolder.create(parent)
            ITEM_TYPE_BUTTON_ADD -> ButtonHolder.create(parent)
            else -> throw IllegalStateException("Unhandled view type")
        }
    }

    private fun isLastElement(position: Int) = position == itemCount - 1

    override fun getItemCount(): Int {
        return super.getItemCount() + 1 // Add one more because there is a button 'add' at the bottom
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLastElement(position)) ITEM_TYPE_BUTTON_ADD else ITEM_TYPE_IMAGE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImageHolder)
            holder.bind(getItem(position), onItemDelete)
        else if (holder is ButtonHolder)
            holder.bind(onAddNewImage)
    }

    class ImageHolder private constructor(private val binding: ItemProductImageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(image: ProductPhoto, onItemDelete: (image: ProductPhoto) -> Unit) {
            binding.apply {
                thumbnail.loadImage(image.urlOrUri)
                imageType.text = if (image.type == UploadPhotoDialog.Mode.EXTERNAL)
                    binding.root.context.getText(R.string.label_external)
                else
                    binding.root.context.getText(R.string.label_internal)

                delete.setOnClickListener { onItemDelete(image) }
            }
        }

    companion object {
        fun create(viewGroup: ViewGroup): ImageHolder {
            val binding = DataBindingUtil.inflate<ItemProductImageBinding>(
                LayoutInflater.from(viewGroup.context),
                R.layout.item_product_image,
                viewGroup,
                false
            )
            return ImageHolder(binding)
        }
    }
}

class ButtonHolder private constructor(private val view: View) : RecyclerView.ViewHolder(view) {
    fun bind(onClick: () -> Unit) {
        view.findViewById<Button>(R.id.add).setOnClickListener { onClick() }
    }

    companion object {
        fun create(viewGroup: ViewGroup): ButtonHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_button_add, viewGroup, false)
            return ButtonHolder(view)
        }
    }
}
}