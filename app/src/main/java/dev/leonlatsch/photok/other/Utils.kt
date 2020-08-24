package dev.leonlatsch.photok.other

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore

fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
    contentResolver.query(uri, projection, null, null, null)?.use {
        if (it.moveToFirst()) {
            return it.getString(0)

        }
    }
    return null
}

fun emptyString(): String {
    return ""
}