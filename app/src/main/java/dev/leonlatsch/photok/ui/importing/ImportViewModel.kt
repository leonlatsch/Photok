package dev.leonlatsch.photok.ui.importing

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import dev.leonlatsch.photok.model.repositories.PhotoRepository

class ImportViewModel @ViewModelInject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    fun importImages(images: MutableList<Uri>) {
        for (img in images) {
            // Use photoRepo to import async
            println(img)
        }
    }
}