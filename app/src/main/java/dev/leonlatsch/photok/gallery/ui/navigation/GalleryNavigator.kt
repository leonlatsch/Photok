/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.gallery.ui.navigation

import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import dev.leonlatsch.photok.backup.ui.RestoreBackupDialogFragment
import dev.leonlatsch.photok.gallery.ui.importing.ImportBottomSheetDialogFragment
import dev.leonlatsch.photok.model.repositories.ImportSource
import dev.leonlatsch.photok.other.extensions.show
import javax.inject.Inject

class GalleryNavigator @Inject constructor() {

    fun navigate(
        event: GalleryNavigationEvent,
        fragment: Fragment,
    ) {
        when (event) {
            is GalleryNavigationEvent.ShowToast -> showToast(event, fragment)
            is GalleryNavigationEvent.StartImport -> startImport(event.fileUris, fragment.childFragmentManager, event.importSource)
            is GalleryNavigationEvent.StartRestoreBackup -> startRestoreBackup(event.backupUri, fragment.childFragmentManager)
        }
    }

    private fun startRestoreBackup(backupUri: Uri, fragmentManager: FragmentManager) {
        RestoreBackupDialogFragment(
            uri = backupUri
        ).show(fragmentManager)
    }

    private fun startImport(fileUris: List<Uri>, fragmentManager: FragmentManager, importSource: ImportSource) {
        ImportBottomSheetDialogFragment(
            uris = fileUris,
            albumUUID = null,
            importSource = importSource,
        ).show(fragmentManager)
    }

    private fun showToast(event: GalleryNavigationEvent.ShowToast, fragment: Fragment) {
        fragment.context?.let { context ->
            Toast.makeText(context, event.text, Toast.LENGTH_LONG).show()
        }
    }
}