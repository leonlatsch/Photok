package dev.leonlatsch.photok.model.repositories

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.dao.PhotoDao
import dev.leonlatsch.photok.security.EncryptionManager
import javax.inject.Inject

class PhotoRepository @Inject constructor(
    private val photoDao: PhotoDao,
    private val encryptionManager: EncryptionManager
) {

    // Database
    suspend fun insert(photo: Photo) = photoDao.insert(photo)

    suspend fun insertAll(photos: List<Photo>) = photoDao.insertAll(photos)

    suspend fun delete(photo: Photo) = photoDao.delete(photo)

    fun getAllPaged() = photoDao.getAllPagedSortedByImportedAt()

    // Filesystem
    fun writePhotoData(context: Context, id: Long, bytes: ByteArray) {
        val encryptedBytes = encryptionManager.encrypt(bytes)
        context.openFileOutput("${id}.photok", Context.MODE_PRIVATE).use {
            it.write(encryptedBytes)
        }
    }

    fun readPhotoFromExternal(contentResolver: ContentResolver, imageUri: Uri): ByteArray? {
        return contentResolver.openInputStream(imageUri)?.readBytes()
    }

    fun readPhotoData(context: Context, id: Long): ByteArray {
        val fileOutputStream = context.openFileInput("${id}.photok")
        val encryptedBytes = fileOutputStream.readBytes()
        return encryptionManager.decrypt(encryptedBytes)
    }
}