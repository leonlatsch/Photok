/*
 *   Copyright 2020-2023 Leon Latsch
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

package dev.leonlatsch.photok.cgallery.ui.navigation

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.ui.importing.ImportMenuDialog
import dev.leonlatsch.photok.other.INTENT_PHOTO_UUID
import dev.leonlatsch.photok.other.extensions.show
import javax.inject.Inject

class GalleryNavigator @Inject constructor() {

    fun navigate(
        event: GalleryNavigationEvent,
        navController: NavController,
        fragmentManager: FragmentManager
    ) {
        when (event) {
            is GalleryNavigationEvent.OpenPhoto -> navigateOpenPhoto(event.photoUUID, navController)
            GalleryNavigationEvent.OpenImportMenu -> navigateOpenImportMenu(fragmentManager)
        }
    }

    private fun navigateOpenImportMenu(fragmentManager: FragmentManager) {
        ImportMenuDialog().show(fragmentManager)
    }

    private fun navigateOpenPhoto(photoUUID: String, navController: NavController) {
        val args = bundleOf(INTENT_PHOTO_UUID to photoUUID)
        navController.navigate(R.id.action_cgalleryFragment_to_imageViewerFragment, args)
    }
}