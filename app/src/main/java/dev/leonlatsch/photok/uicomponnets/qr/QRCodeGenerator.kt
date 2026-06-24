/*
 *   Copyright 2020-2026 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.leonlatsch.photok.uicomponnets.qr

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object QRCodeGenerator {

    suspend fun generateQRCode(
        text: String,
        size: Int = 512,
        foregroundColor: Int,
        backgroundColor: Int,
        errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M,
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel)
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 1)
            }

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints)
            convertBitMatrixToBitmap(bitMatrix, foregroundColor, backgroundColor)
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun convertBitMatrixToBitmap(
        matrix: BitMatrix,
        foregroundColor: Int,
        backgroundColor: Int,
    ): Bitmap {
        return suspendCoroutine { continuation ->

            val width = matrix.width
            val height = matrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (matrix[x, y]) foregroundColor else backgroundColor
                }
            }

            val bitmap = createBitmap(width, height).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
            continuation.resume(bitmap)
        }
    }
}