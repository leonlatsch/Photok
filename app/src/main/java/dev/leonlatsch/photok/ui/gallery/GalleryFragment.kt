package dev.leonlatsch.photok.ui.gallery

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GalleryFragment : Fragment(R.layout.fragment_gallery) {

    private val viewModel: GalleryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        galleryPhotoGrid.layoutManager = GridLayoutManager(requireContext(), 3)

        val adapter = PhotoAdapter(requireContext(), viewModel.photoRepository)
        galleryPhotoGrid.adapter = adapter
        lifecycleScope.launch {
            viewModel.photos.collectLatest { adapter.submitData(it) }
        }

        fabImport.setOnClickListener {
            findNavController().navigate(R.id.action_galleryFragment_to_importFragment)
        }
    }
}