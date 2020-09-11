package dev.leonlatsch.photok.ui.viewphoto

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.ActivityViewPhotoBinding
import dev.leonlatsch.photok.other.INTENT_PHOTO_ID
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

        viewPhotoToolbar.navigationIcon?.setTint(Color.WHITE)
        setSupportActionBar(viewPhotoToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewModel.photoDrawable.observe(this, {
            viewPhotoImageView.setImageBitmap(it)
        })

        initializeSystemUI()
        loadPhoto()
    }

    private val onClickListener = View.OnClickListener {
        toggleSystemUI()
    }

    private fun initializeSystemUI() {
        window.statusBarColor = getColor(android.R.color.black)
        window.navigationBarColor = getColor(android.R.color.black)

        toggleSystemUI()

        window.decorView.setOnSystemUiVisibilityChangeListener {
            if (it and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                viewPhotoAppBarLayout.visibility = View.VISIBLE
            } else {
                viewPhotoAppBarLayout.visibility = View.GONE
            }
        }
    }

    private fun toggleSystemUI() {
        val uiOptions: Int = window.decorView.systemUiVisibility
        var newUiOptions = uiOptions

        newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_FULLSCREEN
        newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_IMMERSIVE

        window.decorView.systemUiVisibility = newUiOptions
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
        binding.onClickListener = onClickListener
    }
}