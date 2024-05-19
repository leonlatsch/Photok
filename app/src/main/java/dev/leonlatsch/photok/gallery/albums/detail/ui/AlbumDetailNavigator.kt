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

package dev.leonlatsch.photok.gallery.albums.detail.ui

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import javax.inject.Inject

class AlbumDetailNavigator @Inject constructor() {

    fun navigate(event: NavigationEvent, fragment: Fragment) {
        when (event) {
            NavigationEvent.Close -> fragment.findNavController().navigateUp()
            is NavigationEvent.ShowToast -> showToast(event, fragment)
        }
    }

    private fun showToast(event: NavigationEvent.ShowToast, fragment: Fragment) {
        fragment.context?.let { context ->
            Toast.makeText(context, event.text, Toast.LENGTH_LONG).show()
        }
    }

    sealed interface NavigationEvent {
        data object Close : NavigationEvent
        data class ShowToast(val text: String) : NavigationEvent
    }
}

