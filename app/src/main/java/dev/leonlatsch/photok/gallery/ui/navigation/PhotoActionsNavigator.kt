


package dev.leonlatsch.photok.gallery.ui.navigation

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import dev.leonlatsch.photok.gallery.ui.menu.DeleteBottomSheetDialogFragment
import dev.leonlatsch.photok.gallery.ui.menu.ExportBottomSheetDialogFragment
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerFragmentDirections
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.other.extensions.show
import javax.inject.Inject

class PhotoActionsNavigator @Inject constructor() {
    fun navigate(action: PhotoAction, navController: NavController, fragment: Fragment) {
        when (action) {
            is PhotoAction.DeletePhotos -> confirmAndDelete(
                action.photos,
                fragment.childFragmentManager
            )

            is PhotoAction.ExportPhotos -> confirmAndExport(
                action.photos,
                action.target,
                fragment.childFragmentManager
            )

            is PhotoAction.OpenPhoto -> navigateOpenPhoto(action.photoUUID, action.albumUUID, navController)
        }
    }

    private fun confirmAndExport(
        photos: List<Photo>,
        target: Uri,
        fragmentManager: FragmentManager,
    ) {
        ExportBottomSheetDialogFragment(photos, target).show(fragmentManager)
    }

    private fun confirmAndDelete(
        photos: List<Photo>,
        fragmentManager: FragmentManager
    ) {
        DeleteBottomSheetDialogFragment(photos).show(fragmentManager)
    }

    private fun navigateOpenPhoto(photoUUID: String, albumUUID: String, navController: NavController) {
        val direction = ImageViewerFragmentDirections.actionGlobalImageViewerFragment(photoUuid = photoUUID, albumUuid = albumUUID)
        navController.navigate(direction)
    }
}

sealed interface PhotoAction {
    data class OpenPhoto(val photoUUID: String, val albumUUID: String = "") : PhotoAction
    data class DeletePhotos(val photos: List<Photo>) : PhotoAction
    data class ExportPhotos(val photos: List<Photo>, val target: Uri) : PhotoAction
}

package dev.leonlatsch.photok.gallery.ui.navigation

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import dev.leonlatsch.photok.gallery.ui.menu.DeleteBottomSheetDialogFragment
import dev.leonlatsch.photok.gallery.ui.menu.ExportBottomSheetDialogFragment
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerFragmentDirections
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.other.extensions.show
import javax.inject.Inject

class PhotoActionsNavigator @Inject constructor() {
    fun navigate(action: PhotoAction, navController: NavController, fragment: Fragment) {
        when (action) {
            is PhotoAction.DeletePhotos -> confirmAndDelete(
                action.photos,
                fragment.childFragmentManager
            )

            is PhotoAction.ExportPhotos -> confirmAndExport(
                action.photos,
                action.target,
                fragment.childFragmentManager
            )

            is PhotoAction.OpenPhoto -> navigateOpenPhoto(action.photoUUID, action.albumUUID, navController)
        }
    }

    private fun confirmAndExport(
        photos: List<Photo>,
        target: Uri,
        fragmentManager: FragmentManager,
    ) {
        ExportBottomSheetDialogFragment(photos, target).show(fragmentManager)
    }

    private fun confirmAndDelete(
        photos: List<Photo>,
        fragmentManager: FragmentManager
    ) {
        DeleteBottomSheetDialogFragment(photos).show(fragmentManager)
    }

    private fun navigateOpenPhoto(photoUUID: String, albumUUID: String, navController: NavController) {
        val direction = ImageViewerFragmentDirections.actionGlobalImageViewerFragment(photoUuid = photoUUID, albumUuid = albumUUID)
        navController.navigate(direction)
    }
}

sealed interface PhotoAction {
    data class OpenPhoto(val photoUUID: String, val albumUUID: String = "") : PhotoAction
    data class DeletePhotos(val photos: List<Photo>) : PhotoAction
    data class ExportPhotos(val photos: List<Photo>, val target: Uri) : PhotoAction
}