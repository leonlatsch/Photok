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

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.ui.importing.ImportMenuDialog
import dev.leonlatsch.photok.gallery.ui.menu.DeleteBottomSheetDialogFragment
import dev.leonlatsch.photok.gallery.ui.menu.ExportBottomSheetDialogFragment
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerFragmentDirections
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.uicomponnets.Dialogs
import javax.inject.Inject

class PhotoActionsNavigator @Inject constructor(
    private val config: Config,
) {
    fun navigate(action: PhotoAction, navController: NavController, fragment: Fragment) {
        when (action) {
            is PhotoAction.DeletePhotos -> confirmAndDelete(
                fragment.requireContext(),
                action.photos,
                fragment.childFragmentManager
            )

            is PhotoAction.ExportPhotos -> confirmAndExport(
                fragment.requireContext(),
                action.photos,
                fragment.childFragmentManager
            )

            is PhotoAction.OpenPhoto -> navigateOpenPhoto(action.photoUUID, navController)
            PhotoAction.OpenImportMenu -> navigateOpenImportMenu(fragment.childFragmentManager)
        }
    }

    private fun navigateOpenImportMenu(fragmentManager: FragmentManager) {
        ImportMenuDialog().show(fragmentManager)
    }

    private fun confirmAndExport(
        context: Context,
        photos: List<Photo>,
        fragmentManager: FragmentManager,
    ) {
        val confirmationText = if (config.deleteExportedFiles) {
            context.getString(R.string.export_and_delete_are_you_sure)
        } else {
            context.getString(R.string.export_are_you_sure)
        }

        Dialogs.showConfirmDialog(
            context,
            String.format(
                confirmationText,
                photos.size
            )
        ) { _, _ -> // On positive button clicked
            ExportBottomSheetDialogFragment(photos).show(fragmentManager)
        }
    }

    private fun confirmAndDelete(
        context: Context,
        photos: List<Photo>,
        fragmentManager: FragmentManager
    ) {
        Dialogs.showConfirmDialog(
            context,
            String.format(
                context.getString(R.string.delete_are_you_sure),
                photos.size
            )
        ) { _, _ -> // On positive button clicked
            DeleteBottomSheetDialogFragment(photos).show(fragmentManager)
        }
    }

    private fun navigateOpenPhoto(photoUUID: String, navController: NavController) {
        val direction = ImageViewerFragmentDirections.actionGlobalImageViewerFragment(photoUuid = photoUUID)
        navController.navigate(direction)
    }
}

sealed interface PhotoAction {
    data class OpenPhoto(val photoUUID: String) : PhotoAction
    data class DeletePhotos(val photos: List<Photo>) : PhotoAction
    data class ExportPhotos(val photos: List<Photo>) : PhotoAction
    data object OpenImportMenu : PhotoAction
}