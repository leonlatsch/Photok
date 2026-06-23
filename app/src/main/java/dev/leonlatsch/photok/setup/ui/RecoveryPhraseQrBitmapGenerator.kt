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

package dev.leonlatsch.photok.setup.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import dev.leonlatsch.photok.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal object RecoveryPhraseQrBitmapGenerator {

    // Preview shown inside the sheet — just the QR code, square
    private const val PREVIEW_SIZE = 420
    private const val PREVIEW_PADDING = 20

    // Document saved to disk
    private const val DOC_WIDTH = 600
    private const val DOC_QR_SIZE = 380
    private const val ICON_SIZE = 80

    // Card geometry: white rounded rect containing the QR code
    private const val CARD_LEFT = 44f
    private const val CARD_TOP = 205f
    private const val CARD_BOTTOM = 621f  // CARD_TOP + DOC_QR_SIZE + 36

    private val COLOR_BG = Color.parseColor("#0D1B2A")
    private val COLOR_TEAL = Color.parseColor("#50C9C3")
    private val COLOR_MUTED = Color.parseColor("#8FA3B0")

    // ── Public API ───────────────────────────────────────────────────────────

    fun generateQrOnly(mnemonicString: String): Bitmap {
        val qrSize = PREVIEW_SIZE - 2 * PREVIEW_PADDING
        val bitmap = createBitmap(PREVIEW_SIZE, PREVIEW_SIZE)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)
        val qr = renderQrMatrix(mnemonicString, qrSize)
        canvas.drawBitmap(qr, PREVIEW_PADDING.toFloat(), PREVIEW_PADDING.toFloat(), null)
        qr.recycle()
        return bitmap
    }

    fun generateDocument(context: Context, mnemonicString: String): Bitmap {
        val words = mnemonicString.split("-")
        val rowCount = (words.size + 3) / 4
        val wordStartY = CARD_BOTTOM + 58f
        val footerLineY = wordStartY + rowCount * 26f + 28f
        val totalHeight = (footerLineY + 50f).toInt()

        val bitmap = createBitmap(DOC_WIDTH, totalHeight)
        val canvas = Canvas(bitmap)
        canvas.drawColor(COLOR_BG)

        drawHeader(context, canvas)
        drawQrCard(canvas, mnemonicString)
        drawMnemonic(canvas, words, wordStartY)
        drawFooter(canvas, footerLineY)

        return bitmap
    }

    // ── Private drawing helpers ──────────────────────────────────────────────

    private fun drawHeader(context: Context, canvas: Canvas) {
        val iconBitmap = renderIconBitmap(context)
        if (iconBitmap != null) {
            canvas.drawBitmap(iconBitmap, (DOC_WIDTH - ICON_SIZE) / 2f, 28f, null)
            iconBitmap.recycle()
        }

        canvas.drawText(
            "Photok", DOC_WIDTH / 2f, 144f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.08f
            },
        )

        canvas.drawLine(
            80f, 164f, (DOC_WIDTH - 80).toFloat(), 164f,
            Paint().apply { color = COLOR_TEAL; alpha = 70; strokeWidth = 1.5f },
        )

        canvas.drawText(
            "RECOVERY PHRASE", DOC_WIDTH / 2f, 190f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_TEAL
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.25f
            },
        )
    }

    private fun drawQrCard(canvas: Canvas, mnemonicString: String) {
        canvas.drawRoundRect(
            RectF(CARD_LEFT, CARD_TOP, DOC_WIDTH - CARD_LEFT, CARD_BOTTOM),
            20f, 20f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE },
        )

        val qr = renderQrMatrix(mnemonicString, DOC_QR_SIZE)
        val qrLeft = (DOC_WIDTH - DOC_QR_SIZE) / 2f
        val qrTop = CARD_TOP + (CARD_BOTTOM - CARD_TOP - DOC_QR_SIZE) / 2f
        canvas.drawBitmap(qr, qrLeft, qrTop, null)
        qr.recycle()
    }

    private fun drawMnemonic(canvas: Canvas, words: List<String>, startY: Float) {
        canvas.drawText(
            "YOUR RECOVERY WORDS", DOC_WIDTH / 2f, startY - 18f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_MUTED
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.2f
            },
        )

        val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEAL
            textSize = 13f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.RIGHT
        }
        val wordPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 13f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.LEFT
        }

        // 4 columns with 30px side margin
        val colWidth = (DOC_WIDTH - 60f) / 4f
        words.forEachIndexed { idx, word ->
            val col = idx % 4
            val row = idx / 4
            val cx = 30f + col * colWidth + colWidth / 2f
            val y = startY + row * 26f
            canvas.drawText("${idx + 1}.", cx - 4f, y, numPaint)
            canvas.drawText(word, cx + 4f, y, wordPaint)
        }
    }

    private fun drawFooter(canvas: Canvas, lineY: Float) {
        canvas.drawLine(
            60f, lineY, (DOC_WIDTH - 60).toFloat(), lineY,
            Paint().apply { color = COLOR_MUTED; alpha = 50; strokeWidth = 1f },
        )
        canvas.drawText(
            "Keep this safe. Never share it.",
            DOC_WIDTH / 2f, lineY + 28f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_MUTED
                textSize = 12f
                textAlign = Paint.Align.CENTER
            },
        )
    }

    private fun renderIconBitmap(context: Context): Bitmap? {
        val drawable = AppCompatResources.getDrawable(context, R.drawable.app_icon) ?: return null
        val bitmap = createBitmap(ICON_SIZE, ICON_SIZE)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, ICON_SIZE, ICON_SIZE)
        drawable.draw(canvas)
        return bitmap
    }

    private fun renderQrMatrix(mnemonicString: String, size: Int): Bitmap {
        val matrix = MultiFormatWriter().encode(mnemonicString, BarcodeFormat.QR_CODE, size, size)
        val pixels = IntArray(size * size) { i ->
            if (matrix[i % size, i / size]) Color.BLACK else Color.WHITE
        }
        return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
    }
}


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
