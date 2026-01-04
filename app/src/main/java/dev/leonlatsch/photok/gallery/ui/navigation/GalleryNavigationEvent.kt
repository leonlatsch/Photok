package dev.leonlatsch.photok.gallery.ui.navigation

import android.net.Uri
import dev.leonlatsch.photok.model.repositories.ImportSource

sealed interface GalleryNavigationEvent {
    data class ShowToast(val text: String) : GalleryNavigationEvent
    data class StartImport(val fileUris: List<Uri>, val importSource: ImportSource) : GalleryNavigationEvent
    data class StartRestoreBackup(val backupUri: Uri) : GalleryNavigationEvent
}