package dev.leonlatsch.photok.ui.importing

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentImportBinding
import dev.leonlatsch.photok.ui.components.BindableFragment
import kotlinx.android.synthetic.main.fragment_import.*

@AndroidEntryPoint
class ImportFragment : BindableFragment<FragmentImportBinding>(R.layout.fragment_import, false) {

    private val viewModel: ImportViewModel by viewModels()
    private val selectPhotos = 0

    var importFinishedText: MutableLiveData<String> = MutableLiveData()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        importFinishedText.postValue(getString(R.string.import_finished))
        viewModel.importState.postValue(ImportState.START)

        viewModel.importState.observe(viewLifecycleOwner, {
            when(it) {
                ImportState.START -> {
                    importStartLayout.visibility = View.VISIBLE
                }
                ImportState.IMPORTING -> {
                    importStartLayout.visibility = View.GONE
                    importImportingAndFinishedLayout.visibility = View.VISIBLE
                    importPercentageLayout.visibility = View.VISIBLE
                }
                ImportState.FINISHED -> {
                    showImportFinished()
                }
                ImportState.ABORTED -> {
                    importFinishedText.postValue(getString(R.string.import_aborted))
                    importFinishedImageView.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.import_aborted))
                    showImportFinished()
                }
                else -> return@observe
            }
        })

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun showImportFinished() {
        importImportingTextView.visibility = View.GONE
        importPercentageLayout.visibility = View.GONE
        importingImageView.visibility = View.GONE
        importFinishedTextView.visibility = View.VISIBLE
        importBackToGalleryButton.visibility = View.VISIBLE
        importFinishedImageView.visibility = View.VISIBLE

        if (viewModel.failed.value!! > 0) {
            importFailedLayout.visibility = View.VISIBLE
        }
    }

    override fun bind(binding: FragmentImportBinding) {
        super.bind(binding)
        binding.viewModel = viewModel
        binding.context = this
    }

    fun importClicked() {
        viewModel.importProgress.postValue(ImportProgress())
        viewModel.importState.postValue(ImportState.START)

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Select Images"), selectPhotos)
    }

    fun backToGalleryClicked() {
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
                viewModel.runImport(images)
            }
        }
    }
}