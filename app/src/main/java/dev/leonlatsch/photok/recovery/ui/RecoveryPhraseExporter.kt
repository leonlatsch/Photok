/*
 *   Copyright 2020–2026 Leon Latsch
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

package dev.leonlatsch.photok.recovery.ui

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.io.IO
import dev.leonlatsch.photok.other.extensions.writeTo
import dev.leonlatsch.photok.uicomponnets.qr.QRCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import javax.inject.Inject

private const val SHARE_DIR = "recovery"
private const val IMAGE_FILE_NAME = "photok-recovery-phrase.jpg"
private const val PHOTOS_SUB_DIR = "Photok"
private const val ACTION_SHARE_TARGET_CHOSEN = "dev.leonlatsch.photok.RECOVERY_PHRASE_SHARE_TARGET_CHOSEN"

class RecoveryPhraseExporter @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val io: IO,
) {

    val sharedTo: Flow<RecoveryPhraseSavedTo.Shared> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val component = IntentCompat.getParcelableExtra(
                    intent,
                    Intent.EXTRA_CHOSEN_COMPONENT,
                    ComponentName::class.java,
                )
                trySend(RecoveryPhraseSavedTo.Shared(component?.let { appLabelOf(it) }))
            }
        }

        ContextCompat.registerReceiver(
            appContext,
            receiver,
            IntentFilter(ACTION_SHARE_TARGET_CHOSEN),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        awaitClose { appContext.unregisterReceiver(receiver) }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun saveImageToPhotos(context: Context, phrase: RecoveryPhrase): RecoveryPhraseSavedTo.Photos? =
        withContext(Dispatchers.IO) {
            val documentBitmap = renderDocument(context, phrase) ?: return@withContext null
            val uri = io.saveImageToPictures(
                fileName = IMAGE_FILE_NAME,
                mimeType = "image/jpeg",
                subDir = PHOTOS_SUB_DIR,
            ) { output ->
                if (!documentBitmap.writeTo(output)) throw IOException("Could not encode recovery phrase image")
            }
            documentBitmap.recycle()

            uri?.let { RecoveryPhraseSavedTo.Photos }
        }

    suspend fun saveImageFile(context: Context, uri: Uri, phrase: RecoveryPhrase): RecoveryPhraseSavedTo.File? =
        withContext(Dispatchers.IO) {
            val documentBitmap = renderDocument(context, phrase) ?: return@withContext null
            val written = try {
                io.openFileOutput(uri)?.use { documentBitmap.writeTo(it) } ?: false
            } catch (e: IOException) {
                Timber.e(e, "Error saving recovery phrase image to $uri")
                false
            }
            documentBitmap.recycle()

            if (written) RecoveryPhraseSavedTo.File(io.getFileName(uri)) else null
        }

    suspend fun shareImage(context: Context, phrase: RecoveryPhrase) {
        val imageFile = withContext(Dispatchers.IO) {
            clearShareCache()
            val documentBitmap = renderDocument(context, phrase) ?: return@withContext null
            val file = File(File(appContext.cacheDir, SHARE_DIR).apply { mkdirs() }, IMAGE_FILE_NAME)
            val written = try {
                file.outputStream().use { documentBitmap.writeTo(it) }
            } catch (e: IOException) {
                Timber.e(e, "Error writing recovery phrase image for sharing")
                false
            }
            documentBitmap.recycle()
            file.takeIf { written }
        } ?: return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile,
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, null, shareTargetChosenSender()).apply {
            putExtra(
                Intent.EXTRA_EXCLUDE_COMPONENTS,
                arrayOf(ComponentName(context, "dev.leonlatsch.photok.SendLauncher")),
            )
        }

        context.startActivity(chooser)
    }

    suspend fun saveTextFile(uri: Uri, phrase: RecoveryPhrase): RecoveryPhraseSavedTo.File? =
        withContext(Dispatchers.IO) {
            val inputStream = ByteArrayInputStream(phrase.toMnemonicString().toByteArray())
            val outputStream = io.openFileOutput(uri) ?: return@withContext null

            io.copy(inputStream, outputStream)
                .onFailure { Timber.e(it, "Error saving recovery phrase text to $uri") }
                .getOrNull() ?: return@withContext null

            RecoveryPhraseSavedTo.File(io.getFileName(uri))
        }

    suspend fun copyToClipboard(clipboard: Clipboard, phrase: RecoveryPhrase): RecoveryPhraseSavedTo.Clipboard {
        val clipData = ClipData.newPlainText(
            "photok-recovery-phrase",
            phrase.toMnemonicString()
        )
        clipboard.setClipEntry(ClipEntry(clipData))
        return RecoveryPhraseSavedTo.Clipboard
    }

    fun clearShareCache() {
        File(appContext.cacheDir, SHARE_DIR).deleteRecursively()
    }

    private suspend fun renderDocument(context: Context, phrase: RecoveryPhrase): Bitmap? {
        val qrBitmap = QRCodeGenerator.generateQRCode(
            text = phrase.toMnemonicString(),
            size = 512,
            foregroundColor = Color.BLACK,
            backgroundColor = Color.WHITE,
        ) ?: return null

        val documentBitmap = createRecoveryPhraseDocument(context, qrBitmap, phrase)
        qrBitmap.recycle()
        return documentBitmap
    }

    private fun appLabelOf(component: ComponentName): String? = try {
        val packageManager = appContext.packageManager
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(component.packageName, 0)).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    private fun shareTargetChosenSender(): IntentSender {
        val callbackIntent = Intent(ACTION_SHARE_TARGET_CHOSEN).setPackage(appContext.packageName)
        return PendingIntent.getBroadcast(
            appContext,
            0,
            callbackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        ).intentSender
    }
}
