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

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryPhraseQrSheet(
    phrase: RecoveryPhrase,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel = hiltViewModel<RecoveryPhraseQrViewModel>()
    val context = LocalContext.current

    val saveQrLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/jpeg")) {
        it ?: return@rememberLauncherForActivityResult
        viewModel.handleUiEvent(RecoveryPhraseQrUiEvent.SaveToFile(phrase, context, it))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            QRCodeImage(
                text = phrase.toMnemonicString(),
                backgroundColor = BottomSheetDefaults.ContainerColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
            )

            Button(
                onClick = { saveQrLauncher.launch("photok-recovery-phrase.jpg") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save as Image")
            }
        }
    }
}

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
                    modifier = Modifier.size(48.dp).align(Alignment.Center),
                )
            }
            qrBitmap != null -> {
                Image(
                    bitmap = qrBitmap!!.asImageBitmap(),
                    contentDescription = "QR Code: $text",
                    contentScale = ContentScale.FillWidth,
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
