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

package dev.leonlatsch.photok.gallery.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun rememberMultiSelectionState(items: List<String>) = remember { // TODO: Impl savable
    MultiSelectionState(items)
}

class MultiSelectionState constructor(
    private val allItems: List<String>,
) {

    var isActive = mutableStateOf(false)
    var selectedItems = mutableStateOf(emptyList<String>())

    fun selectAll() {
        isActive.value = true
        selectedItems.value = allItems

    }
    fun cancelSelection() {
        isActive.value = false
        selectedItems.value = emptyList()
    }
    fun selectItem(uuid: String) {
        isActive.value = true
        selectedItems.value += uuid
    }
    fun deselectItem(uuid: String) {
        if (selectedItems.value.size == 1) {
            isActive.value = false
        }

        selectedItems.value -= uuid
    }
}