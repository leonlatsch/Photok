package dev.leonlatsch.photok.ui.importing

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.Bindable
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentImportBinding
import kotlinx.android.synthetic.main.fragment_import.*

@AndroidEntryPoint
class ImportFragment : Fragment() {

    private val viewModel: ImportViewModel by viewModels()
    private val selectPhotos = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.importState.postValue(ImportState.START)

        viewModel.importState.observe(viewLifecycleOwner, {
            when(it) {
                ImportState.START -> {
                    importStartLayout.visibility = View.VISIBLE
                }
                ImportState.IMPORTING -> {
                    importStartLayout.visibility = View.GONE
                    importImportingLayout.visibility = View.VISIBLE
                }
                ImportState.FINISHED -> {
                    importImportingLayout.visibility = View.GONE
                    importFinishedLayout.visibility = View.VISIBLE
                }
                else -> return@observe
            }
        })

        val binding: FragmentImportBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_import, container, false)
        binding.viewModel = viewModel
        binding.importClickListener = importClickListener
        binding.backToGalleryClickListener = backToGalleryClickListener
        binding.lifecycleOwner = this
        return binding.root
    }

    private val importClickListener = View.OnClickListener {
        viewModel.importProgress.postValue(ImportProgress())
        viewModel.importState.postValue(ImportState.START)

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Select Images"), selectPhotos)
    }

    private val backToGalleryClickListener = View.OnClickListener {
        findNavController().navigate(R.id.action_importFragment_to_galleryFragment)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == selectPhotos && resultCode == Activity.RESULT_OK) {
            val images = mutableListOf<Uri>()
            if (data != null) {
                if (data.clipData != null) {
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        images.add(imageUri)
                    }
                } else if (data.data != null) {
                    val imageUri = data.data!!
                    images.add(imageUri)
                }
            }
            if (images.size > 0) {
                viewModel.importImages(requireContext().contentResolver, images)
            }
        }
    }
}