package dev.leonlatsch.photok.ui.viewphoto

import android.app.Application
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toDrawable
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import kotlinx.coroutines.launch

class ViewPhotoViewModel @ViewModelInject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    var photoDrawable: Drawable? = null
    var photo: Photo? = null

    fun loadPhoto(id: Int) = viewModelScope.launch {
        photo = photoRepository.get(id)

        val photoBytes = photoRepository.readPhotoData(app, id)
        photoDrawable = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size).toDrawable(app.resources)
    }
}