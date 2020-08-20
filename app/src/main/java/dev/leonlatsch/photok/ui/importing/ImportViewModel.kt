package dev.leonlatsch.photok.ui.importing

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.getFileName
import kotlinx.coroutines.launch
import java.util.*

class ImportViewModel @ViewModelInject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    var importState: MutableLiveData<ImportState> = MutableLiveData()

    fun importImages(contentResolver: ContentResolver, uris: List<Uri>) = viewModelScope.launch {
        importState.postValue(ImportState.IMPORTING)

        for (image in uris) {
            // Load Bytes
            val photo = load(contentResolver, image) ?: continue

            // Encrypt Bytes
            encrypt(photo)

            //SAVE
            save(photo)
        }

        importState.postValue(ImportState.FINISHED)
    }

    private fun load(contentResolver: ContentResolver, imageUri: Uri): Photo? {
        val fileName = getFileName(contentResolver, imageUri) ?: UUID.randomUUID().toString()

        val type = when(contentResolver.getType(imageUri)) {
            "image/png" -> PhotoType.PNG
            "image/jpeg" -> PhotoType.JPEG
            "image/gif" -> PhotoType.GIF
            else -> PhotoType.UNDEFINED
        }
        if (type == PhotoType.UNDEFINED) return null

        val bytes = contentResolver.openInputStream(imageUri)?.readBytes()
        bytes ?: return null
        val data = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        return Photo(fileName, data, System.currentTimeMillis(), type)
    }

    private fun encrypt(image: Photo) {
        // TODO: Encrypt bytes
    }

    private suspend fun save(photo: Photo) = photoRepository.insert(photo)
}