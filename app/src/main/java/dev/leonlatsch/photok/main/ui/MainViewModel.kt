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

package dev.leonlatsch.photok.main.ui

import android.app.Application
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.gallery.ui.importing.SharedUrisStore
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import javax.inject.Inject

/**
 * ViewModel for the main activity.
 *
 * @since 1.2.4
 * @author Leon Latsch
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    app: Application,
    private val sharedUrisStore: SharedUrisStore,
) : ObservableViewModel(app) {

    fun addUriToSharedUriStore(uri: Uri) = sharedUrisStore.safeAddUri(uri)

    fun consumeSharedUris(): List<Uri> {
        val sharedUris = sharedUrisStore.getUris()
        sharedUrisStore.clear()
        return sharedUris
    }

    fun getUriCountFromStore(): Int = sharedUrisStore.getUriCount()
}