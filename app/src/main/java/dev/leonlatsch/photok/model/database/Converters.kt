package dev.leonlatsch.photok.model.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import dev.leonlatsch.photok.model.database.entity.PhotoType
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun fromPhotoType(photoType: PhotoType): Int = photoType.value

    @TypeConverter
    fun toPhotoType(photoType: Int): PhotoType = PhotoType.fromValue(photoType)

    @TypeConverter
    fun toBitmap(bytes: ByteArray): Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}