package dev.leonlatsch.photok.ui.viewphoto

import android.app.Application
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.core.graphics.drawable.toDrawable
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class ViewPhotoViewModel @ViewModelInject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    var photoDrawable: MutableLiveData<BitmapDrawable> = MutableLiveData()
    var photo: MutableLiveData<Photo> = MutableLiveData()
    var photoSize = 0

    fun loadPhoto(id: Int) = viewModelScope.launch {
        photo.postValue(photoRepository.get(id))

        val photoBytes = photoRepository.readPhotoData(app, id)
        if (photoBytes == null) {
            // TODO: finish activity
            Timber.d("Error reading photo for id: $id")
            return@launch
        }

        photoSize = photoBytes.size
        photoDrawable.postValue(
            BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size).toDrawable(app.resources)
        )
    }
}