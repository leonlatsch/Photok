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