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

package dev.leonlatsch.photok.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.ApplicationState
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.ActivityMainBinding
import dev.leonlatsch.photok.other.REQ_PERM_SHARED_IMPORT
import dev.leonlatsch.photok.other.getBaseApplication
import dev.leonlatsch.photok.other.hide
import dev.leonlatsch.photok.other.show
import dev.leonlatsch.photok.settings.Config
import dev.leonlatsch.photok.ui.components.Dialogs
import dev.leonlatsch.photok.ui.components.bindings.BindableActivity
import dev.leonlatsch.photok.ui.process.ImportBottomSheetDialogFragment
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

/**
 * The main Activity.
 * Holds all fragments and initializes toolbar, menu, etc.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class MainActivity : BindableActivity<ActivityMainBinding>(R.layout.activity_main) {

    @Inject
    override lateinit var config: Config

    private var sharedDataCache: ArrayList<Uri> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.mainToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        getBaseApplication().rawApplicationState.observe(this, {
            if (it == ApplicationState.UNLOCKED && sharedDataCache.isNotEmpty()) {
                confirmAndStartImportShared()
            }
        })

        dispatchIntent()

        binding.mainNavHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.galleryFragment -> binding.mainAppBarLayout.show()
                    else -> binding.mainAppBarLayout.hide()
                }
            }
    }

    private fun dispatchIntent() {
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                if (uri != null) {
                    sharedDataCache.add(uri)
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                if (uris != null) {
                    sharedDataCache.addAll(uris)
                }
            }
        }
    }

    private fun confirmAndStartImportShared() {
        Dialogs.showConfirmDialog(
            this,
            String.format(getString(R.string.import_sharted_question), sharedDataCache.size)
        ) { _, _ ->
            importShared()
        }

    }

    /**
     * Start importing after the overview of photos.
     */
    @AfterPermissionGranted(REQ_PERM_SHARED_IMPORT)
    fun importShared() {
        if (EasyPermissions.hasPermissions(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            sharedDataCache.let {
                ImportBottomSheetDialogFragment(it).show(
                    supportFragmentManager,
                    ImportBottomSheetDialogFragment::class.qualifiedName
                )
            }
            sharedDataCache = arrayListOf()
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.import_permission_rationale),
                REQ_PERM_SHARED_IMPORT,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    /**
     * Starts the action mode on mainToolbar.
     */
    fun startActionMode(callback: ActionMode.Callback): ActionMode? =
        startSupportActionMode(callback)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menuMainItemSettings -> {
            binding.mainNavHostFragment.findNavController()
                .navigate(R.id.action_galleryFragment_to_settingsFragment)
            true
        }
        R.id.menuMainItemLock -> {
            getBaseApplication().lockApp()
            true
        }
        else -> false
    }

    override fun bind(binding: ActivityMainBinding) {
        super.bind(binding)
        binding.context = this
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward result to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}