package com.yurii.foody.screens.admin.products.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yurii.foody.R
import com.yurii.foody.api.Category
import com.yurii.foody.databinding.FragmentEditCreateProductBinding
import com.yurii.foody.ui.UploadPhotoDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.observeOnLifecycle

data class CategoryItem(val id: Int, val name: String) {
    override fun toString(): String {
        return name
    }

    companion object {
        val NoCategory = CategoryItem(-1, "---")
    }
}

fun Category.toCategoryItem(): CategoryItem {
    return CategoryItem(id = this.id, name = this.name)
}

class ProductEditorFragment : Fragment() {
    private val viewModel: ProductEditorViewModel by viewModels { Injector.provideProductEditorViewModel() }
    private val uploadImageDialog: UploadPhotoDialog by lazy { UploadPhotoDialog(requireContext(), requireActivity().activityResultRegistry) }
    private lateinit var binding: FragmentEditCreateProductBinding
    private val imagesListAdapter = ImagesListAdapter(this::onAddNewAdditionalImage, this::onDeleteAdditionalImage)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_create_product, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.additionalImages.adapter = imagesListAdapter
        binding.defaultImage.setOnClickListener {
            uploadImageDialog.show { viewModel.addMainPhoto(it) }
        }

        binding.availability.onChangeListener = {
            viewModel.availability = it
        }

        viewModel.mainPhoto.observeOnLifecycle(viewLifecycleOwner) { image ->
            image?.run {
                when (this) {
                    is UploadPhotoDialog.Result.Internal -> binding.defaultImage.load(this.uri)
                    is UploadPhotoDialog.Result.External -> binding.defaultImage.load(this.url)
                }
            }
        }

        viewModel.additionalImagesFlow.observeOnLifecycle(viewLifecycleOwner) {
            imagesListAdapter.submitList(it)
        }

        observerCategories()
        return binding.root
    }

    private fun onAddNewAdditionalImage() {
        uploadImageDialog.show {
            when (it) {
                is UploadPhotoDialog.Result.External -> viewModel.addAdditionalImage(AdditionalImageData.create(it))
                is UploadPhotoDialog.Result.Internal -> viewModel.addAdditionalImage(AdditionalImageData.create(it))
            }
        }
    }

    private fun askUserToConfirmDeletionImage(callback: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.label_delete_image)
            .setMessage(R.string.message_delete_image_confirmation)
            .setPositiveButton(R.string.label_yes) { _, _ -> callback.invoke() }
            .setNegativeButton(R.string.label_no) { _, _ -> }
            .show()
    }

    private fun onDeleteAdditionalImage(image: AdditionalImageData) {
        askUserToConfirmDeletionImage {
            viewModel.removeAdditionalImage(image)
        }
    }

    private fun observerCategories() {
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, mutableListOf<CategoryItem>())
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.category.adapter = arrayAdapter
        binding.category.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.category = arrayAdapter.getItem(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //Nothing
            }
        }

        viewModel.categories.observeOnLifecycle(viewLifecycleOwner) { categories ->
            arrayAdapter.clear()
            arrayAdapter.add(CategoryItem.NoCategory)
            arrayAdapter.addAll(categories.map { it.toCategoryItem() })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        uploadImageDialog.dismiss()
    }
}