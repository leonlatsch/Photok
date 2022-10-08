/*
 *   Copyright 2020-2022 Leon Latsch
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

class SharedUrisStore {

    private val sharedUris = mutableListOf<Uri>()

    /**
     * Only add uri if its not already in [sharedUris]
     */
    fun safeAddUri(uri: Uri) {
        sharedUris.find { it == uri } ?: sharedUris.add(uri)
    }

    fun getUris(): List<Uri> = sharedUris.toList()

    fun clear() = sharedUris.clear()
}