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

package dev.leonlatsch.photok.gallery.ui.importing

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SharedUrisStore {

    private val sharedUris = MutableStateFlow<List<Uri>>(emptyList())

    /**
     * Only add uri if its not already in [sharedUris]
     */
    fun safeAddUri(uri: Uri) {
        sharedUris.update { it + uri }
    }

    fun observeSharedUris() = sharedUris.asStateFlow()

    fun reset() {
        sharedUris.update { emptyList() }
    }
}