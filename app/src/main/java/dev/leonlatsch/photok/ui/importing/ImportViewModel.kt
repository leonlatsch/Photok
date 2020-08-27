package dev.leonlatsch.photok.ui.importing

import android.app.Application
import android.content.Context
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
    private val app: Application,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    var importState: MutableLiveData<ImportState> = MutableLiveData()
    var importProgress: MutableLiveData<ImportProgress> = MutableLiveData()

    fun runImport(uris: List<Uri>) = viewModelScope.launch {
        var current = 1
        importState.postValue(ImportState.IMPORTING)
        importProgress.value?.update(0, uris.size)

        for (image in uris) {
            // Load Bytes
            import(image)
            importProgress.value?.update(current, uris.size)
            current++
        }

        importState.postValue(ImportState.FINISHED)
    }

    private suspend fun import(imageUri: Uri) {
        val fileName = getFileName(app.contentResolver, imageUri) ?: UUID.randomUUID().toString()

        val type = when(app.contentResolver.getType(imageUri)) {
            "image/png" -> PhotoType.PNG
            "image/jpeg" -> PhotoType.JPEG
            "image/gif" -> PhotoType.GIF
            else -> PhotoType.UNDEFINED
        }
        if (type == PhotoType.UNDEFINED) return

        val bytes = photoRepository.readPhotoFromExternal(app.contentResolver, imageUri)
        bytes ?: return

        val photo = Photo(fileName, System.currentTimeMillis(), type)
        val id = save(photo)
        photoRepository.writePhotoData(app, id, bytes)
    }

    private suspend fun save(photo: Photo) = photoRepository.insert(photo)
}