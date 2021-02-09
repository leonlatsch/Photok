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

package dev.leonlatsch.photok.ui.gallery

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogImportMenuBinding
import dev.leonlatsch.photok.other.REQ_PERM_IMPORT_PHOTOS
import dev.leonlatsch.photok.other.REQ_PERM_RESTORE
import dev.leonlatsch.photok.other.show
import dev.leonlatsch.photok.other.startActivityForResultAndIgnoreTimer
import dev.leonlatsch.photok.ui.backup.ValidateBackupDialogFragment
import dev.leonlatsch.photok.ui.components.bindings.BindableBottomSheetDialogFragment
import dev.leonlatsch.photok.ui.process.ImportBottomSheetDialogFragment
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

/**
 * BottomSheetDialog for showing import options and starting import Dialogs.
 *
 * @since 2.0.0
 * @author Leon Latsch
 */
class ImportMenuDialog :
    BindableBottomSheetDialogFragment<DialogImportMenuBinding>(R.layout.dialog_import_menu) {

    /**
     * Starts the photo import.
     * Starts a chooser for images.
     * May request permission READ_EXTERNAL_STORAGE.
     * Called by ui.
     */
    @AfterPermissionGranted(REQ_PERM_IMPORT_PHOTOS)
    fun startSelectPhotos() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResultAndIgnoreTimer(
                Intent.createChooser(intent, "Select Photos"),
                REQ_CONTENT_PHOTOS
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.import_permission_rationale),
                REQ_PERM_IMPORT_PHOTOS,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    /**
     * Start restoring a backup.
     * Requests permission and shows [ValidateBackupDialogFragment].
     */
    @AfterPermissionGranted(REQ_PERM_RESTORE)
    fun startSelectBackup() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "application/zip"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResultAndIgnoreTimer(
                Intent.createChooser(intent, "Select Backup"),
                REQ_CONTENT_BACKUP
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.import_permission_rationale),
                REQ_PERM_RESTORE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_CONTENT_PHOTOS && resultCode == Activity.RESULT_OK) {
            val images = mutableListOf<Uri>()
            if (data != null) {
                getImportUris(images, data)
            }
            if (images.size > 0) {
                ImportBottomSheetDialogFragment(images).show(requireActivity().supportFragmentManager)
                dismiss()
            }
        } else if (requestCode == REQ_CONTENT_BACKUP && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                data.data ?: return
                ValidateBackupDialogFragment(data.data!!).show(requireActivity().supportFragmentManager)
                dismiss()
            }
        }
    }

    private fun getImportUris(images: MutableList<Uri>, data: Intent): MutableList<Uri> {
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
        return images
    }

    override fun bind(binding: DialogImportMenuBinding) {
        super.bind(binding)
        binding.context = this
    }

    companion object {
        const val REQ_CONTENT_PHOTOS = 0
        const val REQ_CONTENT_BACKUP = 1
        const val REQ_CONTENT_VIDEOS = 2
    }
}