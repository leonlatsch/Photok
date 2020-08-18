package dev.leonlatsch.photok.model.repositories

import dev.leonlatsch.photok.model.database.Photo
import dev.leonlatsch.photok.model.database.PhotoDao
import javax.inject.Inject

class PhotoRepository @Inject constructor(
    private val photoDao: PhotoDao
) {
    suspend fun insertPhoto(photo: Photo) = photoDao.insertPhoto(photo)

    suspend fun deletePhoto(photo: Photo) = photoDao.deletePhoto(photo)

    fun getAllPhotosSortedByImportedAt() = photoDao.getAllPhotosSortedByImportedAt()
}