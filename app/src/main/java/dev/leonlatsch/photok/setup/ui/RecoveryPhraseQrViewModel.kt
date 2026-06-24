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
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.io.IO
import dev.leonlatsch.photok.other.extensions.writeTo
import dev.leonlatsch.photok.uicomponnets.Dialogs
import dev.leonlatsch.photok.uicomponnets.qr.QRCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RecoveryPhraseQrUiEvent {
    data class SaveToFile(
        val phrase: RecoveryPhrase,
        val context: Context,
        val uri: Uri,
    ) : RecoveryPhraseQrUiEvent
}

@HiltViewModel
class RecoveryPhraseQrViewModel @Inject constructor(
    private val io: IO,
) : ViewModel() {

    fun handleUiEvent(event: RecoveryPhraseQrUiEvent) {
        when (event) {
            is RecoveryPhraseQrUiEvent.SaveToFile -> viewModelScope.launch(Dispatchers.IO) {
                val qrBitmap = QRCodeGenerator.generateQRCode(
                    text = event.phrase.toMnemonicString(),
                    size = 512,
                    foregroundColor = Color.BLACK,
                    backgroundColor = Color.WHITE,
                ) ?: return@launch

                val documentBitmap = createRecoveryPhraseDocument(event.context, qrBitmap, event.phrase)
                qrBitmap.recycle()

                val outputStream = io.openFileOutput(event.uri) ?: run {
                    documentBitmap.recycle()
                    return@launch
                }
                outputStream.use { documentBitmap.writeTo(it) }
                documentBitmap.recycle()
                val fileName = io.getFileName(event.uri)
                Dialogs.showLongToast(event.context, "Saved to $fileName")
            }
        }
    }
}
