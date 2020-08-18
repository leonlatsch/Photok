package dev.leonlatsch.photok.ui.importing

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import kotlinx.coroutines.launch

class ImportViewModel @ViewModelInject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    // TEST
    val tempImage: MutableLiveData<String> = MutableLiveData()

    fun importImages(uris: List<Uri>) = viewModelScope.launch {
        val photos = mutableListOf<Photo>()
        for (uri in uris) {
            photos.add(Photo(uri.toString(), System.currentTimeMillis()))
        }

        photoRepository.insertAll(photos)

        //TEST
        if (photos.size == 1) {
            tempImage.postValue(photos[0].uri)
        }
    }
}