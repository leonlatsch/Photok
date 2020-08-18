package dev.leonlatsch.photok.ui.importing

import android.net.Uri
import androidx.lifecycle.ViewModel

class ImportViewModel : ViewModel() {

    fun importImages(images: MutableList<Uri>) {
        for (img in images) {
            println(img)
        }
    }
}