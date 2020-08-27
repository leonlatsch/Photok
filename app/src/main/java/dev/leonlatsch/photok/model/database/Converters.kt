package dev.leonlatsch.photok.model.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.security.EncryptionManager
import java.io.ByteArrayOutputStream

class Converters {

    private val encryptionManager = EncryptionManager.instance

    @TypeConverter
    fun fromPhotoType(photoType: PhotoType): Int = photoType.value

    @TypeConverter
    fun toPhotoType(photoType: Int): PhotoType = PhotoType.fromValue(photoType)

    @TypeConverter
    fun toBitmap(encryptedBytes: ByteArray): Bitmap {
        val bytes = encryptionManager.decrypt(encryptedBytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val bytes = outputStream.toByteArray()
        return encryptionManager.encrypt(bytes)
    }
}