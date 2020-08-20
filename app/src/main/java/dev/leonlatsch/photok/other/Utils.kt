package dev.leonlatsch.photok.other

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore

fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
    contentResolver.query(uri, projection, null, null, null)?.use { metaCursor ->
        if (metaCursor.moveToFirst()) {
            return metaCursor.getString(0)
        }
    }
    return null
}