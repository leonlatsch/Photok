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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import dev.leonlatsch.photok.R
import timber.log.Timber

@Composable
fun QRCodeImage(
    text: String,
    modifier: Modifier = Modifier,
    foregroundColor: Color = LocalContentColor.current,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    size: Int = 512,
) {
    var retryCount by remember { mutableIntStateOf(0) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(text, size, retryCount) {
        if (text.isNotBlank()) {
            isLoading = true
            try {
                val bitmap = QRCodeGenerator.generateQRCode(
                    text = text,
                    size = size,
                    foregroundColor = foregroundColor.toArgb(),
                    backgroundColor = backgroundColor.toArgb(),
                    errorCorrectionLevel = ErrorCorrectionLevel.M
                )
                qrBitmap = bitmap
                if (bitmap == null) {
                    Timber.d("Failed to generate QR code")
                }
            } catch (e: Exception) {
                Timber.d("Error generating QR code: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp).align(Alignment.Center),
                )
            }
            qrBitmap != null -> {
                Image(
                    bitmap = qrBitmap!!.asImageBitmap(),
                    contentDescription = "QR Code: $text",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.keepScreenOn()
                )
            }
            else -> {
                TextButton(
                    onClick = { retryCount++ },
                ) {
                    Text(stringResource(R.string.common_try_again))
                }
            }
        }
    }
}
