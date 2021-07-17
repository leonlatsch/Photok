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

package dev.leonlatsch.photok.gallery.ui.importing

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.backup.ui.RestoreBackupDialogFragment
import dev.leonlatsch.photok.databinding.DialogImportMenuBinding
import dev.leonlatsch.photok.other.REQ_PERM_IMPORT_PHOTOS
import dev.leonlatsch.photok.other.REQ_PERM_IMPORT_VIDEOS
import dev.leonlatsch.photok.other.REQ_PERM_RESTORE
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.uicomponnets.Chooser
import dev.leonlatsch.photok.uicomponnets.bindings.BindableBottomSheetDialogFragment
import pub.devrel.easypermissions.AfterPermissionGranted

/**
 * BottomSheetDialog for showing import options and starting import Dialogs.
 *
 * @since 1.3.0
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
     * Requests permission and shows [RestoreBackupDialogFragment].
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

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_CONTENT_PHOTOS -> dispatchPhotoImportRequest(data)
                REQ_CONTENT_VIDEOS -> dispatchVideosImportRequest(data)
                REQ_CONTENT_BACKUP -> dispatchBackupImportRequest(data)
            }
        }

        dismiss()
    }

    private fun dispatchPhotoImportRequest(data: Intent?) =
        dispatchMediaElementsImportRequest(data)

    private fun dispatchVideosImportRequest(data: Intent?) =
        dispatchMediaElementsImportRequest(data)

    private fun dispatchBackupImportRequest(data: Intent?) = data?.let {
        it.data?.let { uri ->
            RestoreBackupDialogFragment(uri).show(requireActivity().supportFragmentManager)
        }
    }

    private fun dispatchMediaElementsImportRequest(data: Intent?) = data?.let {
        val mediaUris = resolveUrisFromIntent(it)
        if (mediaUris.isNotEmpty()) {
            ImportBottomSheetDialogFragment(mediaUris).show(requireActivity().supportFragmentManager)
        }
    }

    private fun resolveUrisFromIntent(data: Intent): MutableList<Uri> {
        val uris = mutableListOf<Uri>()
        if (data.clipData != null) {
            val count = data.clipData!!.itemCount
            for (i in 0 until count) {
                val uri = data.clipData!!.getItemAt(i).uri
                uris.add(uri)
            }
        } else if (data.data != null) {
            val uri = data.data!!
            uris.add(uri)
        }
        return uris
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