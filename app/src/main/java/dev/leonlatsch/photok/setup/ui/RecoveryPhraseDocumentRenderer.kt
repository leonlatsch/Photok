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
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase

internal fun createRecoveryPhraseDocument(
    context: Context,
    qrBitmap: Bitmap,
    phrase: RecoveryPhrase,
): Bitmap {
    val docWidth = 1200
    val padding = 80
    val sectionGap = 48
    val centerX = docWidth / 2f

    val appNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = ResourcesCompat.getFont(context, R.font.lobster_regular)
        textSize = 96f
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 52f
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
    }

    val phrasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.MONOSPACE
        textSize = 36f
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFCCCCCC.toInt()
        strokeWidth = 2f
    }

    val qrSize = qrBitmap.width
    val phraseLines = phrase.words.chunked(4).map { it.joinToString("-") }

    val appNameH = -appNamePaint.ascent() + appNamePaint.descent()
    val subtitleH = -subtitlePaint.ascent() + subtitlePaint.descent()
    val phraseLineH = (-phrasePaint.ascent() + phrasePaint.descent()) * 1.4f

    val totalHeight = (padding +
        appNameH + sectionGap +
        2f + sectionGap +
        subtitleH + sectionGap +
        qrSize + sectionGap +
        phraseLineH * phraseLines.size +
        padding).toInt()

    val bitmap = Bitmap.createBitmap(docWidth, totalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

    var y = padding.toFloat()

    // App name
    canvas.drawText("Photok", centerX, y - appNamePaint.ascent(), appNamePaint)
    y += appNameH + sectionGap

    // Subtitle
    canvas.drawText(context.getString(R.string.recovery_phrase_document_title), centerX, y - subtitlePaint.ascent(), subtitlePaint)
    y += subtitleH + sectionGap

    // QR code
    canvas.drawBitmap(qrBitmap, (docWidth - qrSize) / 2f, y, null)
    y += qrSize + sectionGap

    // Phrase in monospace, 4 words per line
    for (line in phraseLines) {
        canvas.drawText(line, centerX, y - phrasePaint.ascent(), phrasePaint)
        y += phraseLineH
    }

    return bitmap
}
