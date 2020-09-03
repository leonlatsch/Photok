package dev.leonlatsch.photok.model.repositories

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.dao.PhotoDao
import dev.leonlatsch.photok.security.EncryptionManager
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Repository for [Photo].
 * Uses [PhotoDao] and accesses the filesystem to read and write encrypted photos.
 *
 * @since 1.0.0
 */
class PhotoRepository @Inject constructor(
    private val photoDao: PhotoDao,
    private val encryptionManager: EncryptionManager
) {

    // DATABASE

    suspend fun insert(photo: Photo) = photoDao.insert(photo)

    suspend fun insertAll(photos: List<Photo>) = photoDao.insertAll(photos)

    suspend fun delete(photo: Photo) = photoDao.delete(photo)

    fun getAllPaged() = photoDao.getAllPagedSortedByImportedAt()

    // FILESYSTEM

    /**
     * Write a [ByteArray] of a photo to the filesystem.
     * Also create a thumbnail for it using [createAndWriteThumbnail].
     *
     * @param context used to write file.
     * @param id used for the file name.
     * @param bytes the photo data to save.
     *
     * @since 1.0.0
     */
    fun writePhotoData(context: Context, id: Long, bytes: ByteArray) {
        val encryptedBytes = encryptionManager.encrypt(bytes)
        context.openFileOutput("${id}.photok", Context.MODE_PRIVATE).use {
            it.write(encryptedBytes)
        }
        createAndWriteThumbnail(context, id, bytes)
    }

    /**
     * Create a thumbnail of a photo's bytes.
     *
     * @param context used to write file.
     * @param id used for the file name.
     * @param bytes the full size photo bytes.
     *
     * @since 1.0.0
     */
    private fun createAndWriteThumbnail(context: Context, id: Long, bytes: ByteArray) {
        val thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeByteArray(bytes, 0, bytes.size), THUMBNAIL_SIZE, THUMBNAIL_SIZE)
        val outputStream = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val thumbnailBytes = outputStream.toByteArray()
        val encryptedThumbnailBytes = encryptionManager.encrypt(thumbnailBytes)
        context.openFileOutput("${id}.photok.tn", Context.MODE_PRIVATE).use {
            it.write(encryptedThumbnailBytes)
        }
    }

    /**
     * Read a photo's bytes from external storage.
     */
    fun readPhotoFromExternal(contentResolver: ContentResolver, imageUri: Uri): ByteArray? =
        contentResolver.openInputStream(imageUri)?.readBytes()

    /**
     * Read and decrypt a photo's bytes from internal storage.
     */
    fun readPhotoData(context: Context, id: Int): ByteArray =
        readAndDecryptFile(context, "${id}.photok")

    /**
     * Read and decrypt a photo's thumbnail from internal storage.
     */
    fun readPhotoThumbnailData(context: Context, id: Int): ByteArray =
        readAndDecryptFile(context, "${id}.photok.tn")

    private fun readAndDecryptFile(context: Context, fileName: String): ByteArray {
        val fileInputStream = context.openFileInput(fileName)
        val encryptedBytes = fileInputStream.readBytes()
        return encryptionManager.decrypt(encryptedBytes)
    }

    companion object {
        private const val THUMBNAIL_SIZE = 256
    }
}