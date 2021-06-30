package com.yurii.foody.screens.admin.products.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentEditCreateProductBinding
import com.yurii.foody.ui.UploadPhotoDialog
import com.yurii.foody.utils.observeOnLifecycle

class ProductEditorFragment : Fragment() {
    private val viewModel: ProductEditorViewModel by viewModels { ProductEditorViewModel.Factory() }
    private val uploadImageDialog: UploadPhotoDialog by lazy { UploadPhotoDialog(requireContext(), requireActivity().activityResultRegistry) }
    private lateinit var binding: FragmentEditCreateProductBinding
    private val imagesListAdapter = ImagesListAdapter(this::onAddNewAdditionalImage, this::onDeleteAdditionalImage)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_create_product, container, false)

        binding.additionalImages.adapter = imagesListAdapter
        binding.defaultImage.setOnClickListener {
            uploadImageDialog.show { viewModel.mainPhoto.value = it }
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

        return binding.root
    }

    private fun onAddNewAdditionalImage() {
        uploadImageDialog.show {
            when (it) {
                is UploadPhotoDialog.Result.External -> viewModel.addAdditionalImage(
                    AdditionalImageData.create(
                        it.url,
                        getString(R.string.label_external)
                    )
                )
                is UploadPhotoDialog.Result.Internal -> viewModel.addAdditionalImage(
                    AdditionalImageData.create(
                        it.uri.toString(),
                        getString(R.string.label_internal)
                    )
                )
            }
        }
    }

    private fun onDeleteAdditionalImage(image: AdditionalImageData) {
        viewModel.removeAdditionalImage(image)
    }

    override fun onDestroy() {
        super.onDestroy()
        uploadImageDialog.dismiss()
    }
}