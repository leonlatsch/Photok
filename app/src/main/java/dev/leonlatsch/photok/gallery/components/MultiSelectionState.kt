package dev.leonlatsch.photok.gallery.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun rememberMultiSelectionState(items: List<String>) = remember(items) {
    MultiSelectionState(items)
}

class MultiSelectionState(
    private val allItems: List<String>,
) {

    var isActive = mutableStateOf(false)
    var selectedItems = mutableStateOf(emptyList<String>())
    val showMore = mutableStateOf(false)

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

    fun showMore() {
        showMore.value = true
    }

    fun dismissMore() {
        showMore.value = false
    }
}