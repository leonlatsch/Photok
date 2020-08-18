package dev.leonlatsch.photok.model.repositories

import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.dao.PhotoDao
import javax.inject.Inject

class PhotoRepository @Inject constructor(
    private val photoDao: PhotoDao
) {
    suspend fun insert(photo: Photo) = photoDao.insert(photo)

    suspend fun insertAll(photos: List<Photo>) = photoDao.insertAll(photos)

    suspend fun delete(photo: Photo) = photoDao.delete(photo)

    fun getAllPhotosSortedByImportedAt() = photoDao.getAllPhotosSortedByImportedAt()
}