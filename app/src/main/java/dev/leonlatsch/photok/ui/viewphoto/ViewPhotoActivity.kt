/*
 *   Copyright 2020-2021 Leon Latsch
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
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.ActivityViewPhotoBinding
import dev.leonlatsch.photok.other.*
import dev.leonlatsch.photok.settings.Config
import dev.leonlatsch.photok.ui.components.Dialogs
import dev.leonlatsch.photok.ui.components.bindings.BindableActivity
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

/**
 * Activity to view a photo in full screen mode.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class ViewPhotoActivity : BindableActivity<ActivityViewPhotoBinding>(R.layout.activity_view_photo) {

    private val viewModel: ViewPhotoViewModel by viewModels()

    @Inject
    override lateinit var config: Config

    private var systemUiVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.viewPhotoToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        initializeSystemUI()

        binding.viewPhotoViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.updateDetails(position)
            }
        })

        viewModel.preloadData { ids ->
            val photoPagerAdapter = PhotoPagerAdapter(ids, viewModel.photoRepository, {
                binding.viewPhotoViewPager.isUserInputEnabled = !it // On Zoom changed
            }, {
                toggleSystemUI() // On clicked
            })
            binding.viewPhotoViewPager.adapter = photoPagerAdapter

            val photoId = intent.extras?.get(INTENT_PHOTO_ID)
            val startingAt = if (photoId != null && photoId is Int?) {
                ids.indexOf(photoId)
            } else {
                0
            }
            binding.viewPhotoViewPager.setCurrentItem(startingAt, false)
        }
    }

    /**
     * On Detail button clicked.
     * Called by ui.
     */
    fun onDetails() {
        val detailsBottomSheetDialog =
            DetailsBottomSheetDialog(viewModel.currentPhoto)
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
        if (EasyPermissions.hasPermissions(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ) {
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
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_view_photo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menuViewPhotoInfo -> {
            onDetails()
            true
        }
        else -> false
    }

    @Suppress("DEPRECATION")
    private fun initializeSystemUI() {
        window.statusBarColor = getColor(android.R.color.black)
        window.navigationBarColor = getColor(android.R.color.black)

        window.addSystemUIVisibilityListener {
            systemUiVisible = it
            if (it) {
                binding.viewPhotoToolbar.show()
                binding.viewPhotoBottomToolbarLayout.show()
            } else {
                binding.viewPhotoToolbar.hide()
                binding.viewPhotoBottomToolbarLayout.hide()
            }
        }

        if (config.galleryAutoFullscreen) { // Hide system ui if configured
            toggleSystemUI()
        }
    }

    private fun toggleSystemUI() {
        if (systemUiVisible) {
            hideSystemUI()
        } else {
            showSystemUI()
        }
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