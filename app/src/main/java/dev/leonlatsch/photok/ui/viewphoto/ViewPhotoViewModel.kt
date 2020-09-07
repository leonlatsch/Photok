package dev.leonlatsch.photok.ui.viewphoto

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import kotlinx.coroutines.launch

class ViewPhotoViewModel @ViewModelInject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    var photoDrawable: MutableLiveData<Bitmap> = MutableLiveData()
    var photo: Photo? = null

    fun loadPhoto(id: Int) = viewModelScope.launch {
        photo = photoRepository.get(id)

        val photoBytes = photoRepository.readPhotoData(app, id)
        photoDrawable.postValue(BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size))
    }
}