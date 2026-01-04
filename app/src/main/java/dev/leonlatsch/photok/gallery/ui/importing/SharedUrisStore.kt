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