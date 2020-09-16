package dev.leonlatsch.photok.ui.viewphoto

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.ActivityViewPhotoBinding
import dev.leonlatsch.photok.other.INTENT_PHOTO_ID
import dev.leonlatsch.photok.other.toggleSystemUI
import dev.leonlatsch.photok.ui.components.BindableActivity
import kotlinx.android.synthetic.main.activity_view_photo.*

/**
 * Activity to view a photo in full screen mode.
 *
 * @since 1.0.0
 */
@AndroidEntryPoint
class ViewPhotoActivity : BindableActivity<ActivityViewPhotoBinding>(R.layout.activity_view_photo) {

    private val viewModel: ViewPhotoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(viewPhotoToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        initializeSystemUI()
        loadPhoto()
    }

    fun onClick() {
        toggleSystemUI(window)
    }

    fun onDetails() {
        val detailView = layoutInflater.inflate(R.layout.view_photo_detail, viewPhotoLayout, false)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(detailView)
        dialog.show()
    }

    fun onDelete() {
        //TODO
    }

    fun onExport() {
        //TODO
    }

    private fun initializeSystemUI() {
        window.statusBarColor = getColor(android.R.color.black)
        window.navigationBarColor = getColor(android.R.color.black)

        toggleSystemUI(window)

        window.decorView.setOnSystemUiVisibilityChangeListener {
            if (it and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                viewPhotoAppBarLayout.visibility = View.VISIBLE
                viewPhotoBottomToolbarLayout.visibility = View.VISIBLE
            } else {
                viewPhotoAppBarLayout.visibility = View.GONE
                viewPhotoBottomToolbarLayout.visibility = View.GONE
            }
        }
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun bind(binding: ActivityViewPhotoBinding) {
        super.bind(binding)
        binding.viewModel = viewModel
        binding.context = this
    }
}