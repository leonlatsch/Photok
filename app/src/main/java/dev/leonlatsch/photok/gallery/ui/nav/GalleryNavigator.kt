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

package dev.leonlatsch.photok.gallery.ui.nav

import androidx.fragment.app.FragmentManager
import dev.leonlatsch.photok.gallery.ui.GalleryFragment
import dev.leonlatsch.photok.gallery.ui.PhotoAdapter
import dev.leonlatsch.photok.gallery.ui.collections.AddCollectionBottomSheetDialogFragment
import dev.leonlatsch.photok.gallery.ui.importing.ImportMenuDialog
import dev.leonlatsch.photok.gallery.ui.menu.DeleteBottomSheetDialogFragment
import dev.leonlatsch.photok.news.ui.NewsDialog
import dev.leonlatsch.photok.other.extensions.show
import javax.inject.Inject

class GalleryNavigator @Inject constructor() {

    fun navigate(navigationEvent: NavigationEvent, fragment: GalleryFragment) {
        when (navigationEvent) {
            is NavigationEvent.ShowNewsDialog -> navigateShowNewsDialog(fragment.childFragmentManager)
            is NavigationEvent.ShowImportMenu -> navigateShowImportMenu(fragment.childFragmentManager)
            is NavigationEvent.ShowAddCollectionDialog -> navigateCreateCollectionDialog(fragment.childFragmentManager)
        }
    }

    private fun navigateShowNewsDialog(fragmentManager: FragmentManager) {
        NewsDialog().show(fragmentManager)
    }

    private fun navigateShowImportMenu(fragmentManager: FragmentManager) {
        ImportMenuDialog().show(fragmentManager)
    }

    private fun navigateCreateCollectionDialog(fragmentManager: FragmentManager) {
        AddCollectionBottomSheetDialogFragment().show(fragmentManager)
    }
}