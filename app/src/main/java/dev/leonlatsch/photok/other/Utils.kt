package dev.leonlatsch.photok.other

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.view.View

fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
    contentResolver.query(uri, projection, null, null, null)?.use {
        if (it.moveToFirst()) {
            return it.getString(0)

        }
    }
    return null
}

fun hideLoadingOverlay(overlay: View?) {
    overlay?.visibility = View.GONE
}

fun showLoadingOverlay(overlay: View?) {
    overlay?.visibility = View.VISIBLE
}

fun emptyString(): String {
    return ""
}