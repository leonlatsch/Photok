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
import dev.leonlatsch.photok.other.REQ_PERM_IMPORT_VIDEOS
import dev.leonlatsch.photok.other.REQ_PERM_RESTORE
import dev.leonlatsch.photok.other.show
import dev.leonlatsch.photok.ui.backup.ValidateBackupDialogFragment
import dev.leonlatsch.photok.ui.components.Chooser
import dev.leonlatsch.photok.ui.components.bindings.BindableBottomSheetDialogFragment
import dev.leonlatsch.photok.ui.process.ImportBottomSheetDialogFragment
import pub.devrel.easypermissions.AfterPermissionGranted

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
    fun startSelectPhotos() = Chooser.Builder()
        .message("Select Photos")
        .mimeType("image/*")
        .allowMultiple()
        .requestCode(REQ_CONTENT_PHOTOS)
        .permissionCode(REQ_PERM_IMPORT_PHOTOS)
        .permission(Manifest.permission.READ_EXTERNAL_STORAGE)
        .show(this)

    /**
     * Start the video import.
     * Starts a chooser for videos.
     * May request permission READ_EXTERNAL_STORAGE.
     * Called by ui.
     */
    @AfterPermissionGranted(REQ_PERM_IMPORT_VIDEOS)
    fun startSelectVideos() = Chooser.Builder()
        .message("Select Videos")
        .mimeType("video/*")
        .allowMultiple()
        .requestCode(REQ_CONTENT_VIDEOS)
        .permissionCode(REQ_PERM_IMPORT_VIDEOS)
        .permission(Manifest.permission.READ_EXTERNAL_STORAGE)
        .show(this)

    /**
     * Start restoring a backup.
     * Requests permission and shows [ValidateBackupDialogFragment].
     */
    @AfterPermissionGranted(REQ_PERM_RESTORE)
    fun startSelectBackup() = Chooser.Builder()
        .message("Select Backup")
        .mimeType("application/zip")
        .requestCode(REQ_CONTENT_BACKUP)
        .permissionCode(REQ_PERM_RESTORE)
        .permission(Manifest.permission.READ_EXTERNAL_STORAGE)
        .show(this)

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