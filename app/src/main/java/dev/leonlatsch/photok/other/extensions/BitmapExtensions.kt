package dev.leonlatsch.photok.other.extensions

import android.graphics.Bitmap
import java.io.OutputStream

fun Bitmap.writeTo(out: OutputStream) {
    compress(Bitmap.CompressFormat.JPEG, 100, out)
}
