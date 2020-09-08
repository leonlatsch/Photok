package dev.leonlatsch.photok.ui.viewphoto

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.ActivityViewPhotoBinding
import dev.leonlatsch.photok.other.INTENT_PHOTO_ID
import dev.leonlatsch.photok.ui.components.BindableActivity
import kotlinx.android.synthetic.main.activity_view_photo.*

@AndroidEntryPoint
class ViewPhotoActivity : BindableActivity<ActivityViewPhotoBinding>(R.layout.activity_view_photo) {

    private val viewModel: ViewPhotoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.photoDrawable.observe(this, {
            viewPhotoImageView.setImageBitmap(it)
        })
        loadPhoto()
    }

    private fun loadPhoto() {
        val id = intent.extras?.get(INTENT_PHOTO_ID)
        if (id != null && id is Int?) {
            viewModel.loadPhoto(id)
        } else {
            closeOnError(id)
        }
    }

    private fun closeOnError(id: Any?) {
        Log.e(ViewPhotoActivity::class.toString(), "Error loading photo for id: $id")
    }

    override fun bind(binding: ActivityViewPhotoBinding) {
        super.bind(binding)
        binding.viewModel = viewModel
    }
}