/*
 *   Copyright 2020 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.leonlatsch.photok.ui.viewphoto

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.ActivityViewPhotoBinding
import dev.leonlatsch.photok.other.INTENT_PHOTO_ID
import dev.leonlatsch.photok.other.toggleSystemUI
import dev.leonlatsch.photok.ui.components.BindableActivity
import kotlinx.android.synthetic.main.activity_view_photo.*
import timber.log.Timber

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
        val detailsBottomSheetDialog =
            DetailsBottomSheetDialog(viewModel.photo.value, viewModel.photoSize)
        detailsBottomSheetDialog.show(
            supportFragmentManager,
            DetailsBottomSheetDialog::class.qualifiedName
        )
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
        Timber.d("Error loading photo for id: $id")
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