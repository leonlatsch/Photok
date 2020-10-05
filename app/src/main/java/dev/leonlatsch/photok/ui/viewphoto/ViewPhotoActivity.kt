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

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.ActivityViewPhotoBinding
import dev.leonlatsch.photok.other.*
import dev.leonlatsch.photok.ui.components.BindableActivity
import dev.leonlatsch.photok.ui.components.Dialogs
import kotlinx.android.synthetic.main.activity_view_photo.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

/**
 * Activity to view a photo in full screen mode.
 *
 * @since 1.0.0
 * @author Leon Latsch
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

    /**
     * On Image View Clicked.
     * Called by ui.
     */
    fun onClick() {
        toggleSystemUI(window)
    }

    /**
     * On Detail button clicked.
     * Called by ui.
     */
    fun onDetails() {
        val detailsBottomSheetDialog =
            DetailsBottomSheetDialog(viewModel.photo.value, viewModel.photoSize)
        detailsBottomSheetDialog.show(
            supportFragmentManager,
            DetailsBottomSheetDialog::class.qualifiedName
        )
    }

    /**
     * On delete button clicked.
     * Called by ui.
     */
    fun onDelete() {
        Dialogs.showConfirmDialog(this, getString(R.string.delete_are_you_sure_this)) { _, _ ->
            viewModel.deletePhoto({ // onSuccess
                finish()
            }, { // onError
                Dialogs.showLongToast(this, getString(R.string.common_error))
            })
        }
    }

    /**
     * On export clicked.
     * May request permission WRITE_EXTERNAL_STORAGE.
     * Called by ui.
     */
    @AfterPermissionGranted(REQ_PERM_EXPORT)
    fun onExport() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Dialogs.showConfirmDialog(this, getString(R.string.export_are_you_sure_this)) { _, _ ->
                viewModel.exportPhoto({ // onSuccess
                    Dialogs.showShortToast(this, getString(R.string.export_finished))
                }, { // onError
                    Dialogs.showLongToast(this, getString(R.string.common_error))
                })
            }
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.export_permission_rationale),
                REQ_PERM_EXPORT,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    private fun initializeSystemUI() {
        window.statusBarColor = getColor(android.R.color.black)
        window.navigationBarColor = getColor(android.R.color.black)

        toggleSystemUI(window)

        window.decorView.setOnSystemUiVisibilityChangeListener {
            if (it and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                viewPhotoAppBarLayout.show()
                viewPhotoBottomToolbarLayout.show()
            } else {
                viewPhotoAppBarLayout.hide()
                viewPhotoBottomToolbarLayout.hide()
            }
        }
    }

    private fun loadPhoto() {
        val id = intent.extras?.get(INTENT_PHOTO_ID)
        if (id != null && id is Int?) {
            viewModel.loadPhoto(id) {
                finish() // onError
            }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}