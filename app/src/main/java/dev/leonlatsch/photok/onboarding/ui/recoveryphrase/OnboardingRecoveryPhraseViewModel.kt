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

package dev.leonlatsch.photok.onboarding.ui.recoveryphrase

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.RecoveryPhraseStore
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.io.IO
import dev.leonlatsch.photok.other.AndroidFeatures
import dev.leonlatsch.photok.other.extensions.writeTo
import dev.leonlatsch.photok.recovery.ui.createRecoveryPhraseDocument
import dev.leonlatsch.photok.settings.domain.ScreenshotOverride
import dev.leonlatsch.photok.uicomponnets.qr.QRCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import javax.inject.Inject

private const val SHARE_DIR = "recovery"
private const val QR_IMAGE_FILE_NAME = "photok-recovery-phrase.jpg"
private const val PHOTOS_SUB_DIR = "Photok"
private const val ACTION_SHARE_TARGET_CHOSEN = "dev.leonlatsch.photok.RECOVERY_PHRASE_SHARE_TARGET_CHOSEN"

@HiltViewModel
class OnboardingRecoveryPhraseViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val resources: Resources,
    private val io: IO,
    private val sessionRepository: SessionRepository,
    private val recoveryPhraseStore: RecoveryPhraseStore,
    private val vaultService: VaultService,
    private val screenshotOverride: ScreenshotOverride,
) : ViewModel() {

    private val wordCount = MutableStateFlow(Bip39WordCount.Twelve)
    private val loading = MutableStateFlow(false)
    private val savedTo = MutableStateFlow<RecoveryPhraseSavedTo?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val phrase = sessionRepository.observe().flatMapLatest { session ->
        session?.let { recoveryPhraseStore.observe(it) } ?: flowOf(null)
    }

    val uiState = combine(
        phrase,
        wordCount,
        loading,
        savedTo,
    ) { phrase, wordCount, loading, savedTo ->
        OnboardingRecoveryPhraseUiState(
            phrase = phrase,
            wordCount = wordCount,
            loading = loading,
            savedTo = savedTo,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), OnboardingRecoveryPhraseUiState())

    private val navigationChannel = Channel<OnboardingRecoveryPhraseNavigationEvent>(Channel.UNLIMITED)
    val navigationEvents = navigationChannel.receiveAsFlow()

    private val shareTargetChosenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val component = IntentCompat.getParcelableExtra(
                intent,
                Intent.EXTRA_CHOSEN_COMPONENT,
                ComponentName::class.java,
            )
            savedTo.value = RecoveryPhraseSavedTo.Shared(component?.let { appLabelOf(it) })
        }
    }

    init {
        ContextCompat.registerReceiver(
            appContext,
            shareTargetChosenReceiver,
            IntentFilter(ACTION_SHARE_TARGET_CHOSEN),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        viewModelScope.launch {
            val session = sessionRepository.get() ?: return@launch

            if (!vaultService.isSetup(VaultProtectionType.RecoveryPhrase)) {
                vaultService.create(
                    CreateRequest.RecoveryPhrase(session, Bip39WordCount.Twelve)
                )
            }
        }
    }

    fun handleUiEvent(event: OnboardingRecoveryPhraseUiEvent) {
        when (event) {
            is OnboardingRecoveryPhraseUiEvent.SaveImageToPhotos -> viewModelScope.launch {
                if (!AndroidFeatures.mediaStore()) {
                    navigationChannel.trySend(OnboardingRecoveryPhraseNavigationEvent.PickImageFileLocation)
                    return@launch
                }

                loading.value = true

                val uri = withContext(Dispatchers.IO) {
                    val documentBitmap = renderDocument(event.context, event.phrase) ?: return@withContext null
                    val uri = io.saveImageToPictures(
                        fileName = QR_IMAGE_FILE_NAME,
                        mimeType = "image/jpeg",
                        subDir = PHOTOS_SUB_DIR,
                    ) { documentBitmap.writeTo(it) }
                    documentBitmap.recycle()
                    uri
                }

                loading.value = false

                if (uri == null) {
                    navigationChannel.trySend(OnboardingRecoveryPhraseNavigationEvent.PickImageFileLocation)
                } else {
                    savedTo.value = RecoveryPhraseSavedTo.Photos
                }
            }

            is OnboardingRecoveryPhraseUiEvent.SaveImageFile -> viewModelScope.launch {
                loading.value = true

                val saved = withContext(Dispatchers.IO) {
                    val documentBitmap = renderDocument(event.context, event.phrase) ?: return@withContext false
                    val outputStream = io.openFileOutput(event.uri)
                    outputStream?.use { documentBitmap.writeTo(it) }
                    documentBitmap.recycle()
                    outputStream != null
                }

                loading.value = false

                if (saved) {
                    savedTo.value = RecoveryPhraseSavedTo.File(io.getFileName(event.uri))
                }
            }

            is OnboardingRecoveryPhraseUiEvent.ShareImage -> viewModelScope.launch {
                loading.value = true

                val imageFile = withContext(Dispatchers.IO) {
                    val documentBitmap = renderDocument(event.context, event.phrase) ?: return@withContext null
                    val file = File(File(appContext.cacheDir, SHARE_DIR).apply { mkdirs() }, QR_IMAGE_FILE_NAME)
                    file.outputStream().use { documentBitmap.writeTo(it) }
                    documentBitmap.recycle()
                    file
                }

                loading.value = false
                imageFile ?: return@launch

                val uri = FileProvider.getUriForFile(
                    event.context,
                    "${event.context.packageName}.fileprovider",
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
                        arrayOf(ComponentName(event.context, "dev.leonlatsch.photok.SendLauncher")),
                    )
                }

                event.context.startActivity(chooser)
            }

            is OnboardingRecoveryPhraseUiEvent.SaveTextFile -> viewModelScope.launch(Dispatchers.IO) {
                val inputStream = ByteArrayInputStream(event.phrase.toMnemonicString().toByteArray())
                val outputStream = io.openFileOutput(event.uri) ?: return@launch

                io.copy(inputStream, outputStream)

                savedTo.value = RecoveryPhraseSavedTo.File(io.getFileName(event.uri))
            }

            is OnboardingRecoveryPhraseUiEvent.ShareText -> {
                val text = """
                    Photok ${resources.getString(R.string.recovery_phrase_label)}

                    ${event.phrase.toMnemonicString()}
                """.trimIndent()

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }

                event.context.startActivity(Intent.createChooser(sendIntent, null, shareTargetChosenSender()))
            }

            is OnboardingRecoveryPhraseUiEvent.CopyToClipboard -> viewModelScope.launch {
                val clipData = ClipData.newPlainText(
                    "photok-recovery-phrase",
                    event.phrase.toMnemonicString()
                )
                event.clipboard.setClipEntry(ClipEntry(clipData))
                savedTo.value = RecoveryPhraseSavedTo.Clipboard
            }

            is OnboardingRecoveryPhraseUiEvent.UpdateWordCount -> {
                if (wordCount.value == event.wordCount) return

                val session = sessionRepository.get() ?: return

                recoveryPhraseStore.clear()
                wordCount.value = event.wordCount
                loading.value = true
                savedTo.value = null

                viewModelScope.launch(Dispatchers.IO) {
                    vaultService.create(CreateRequest.RecoveryPhrase(session, event.wordCount))
                    loading.value = false
                }
            }

            OnboardingRecoveryPhraseUiEvent.AllowScreenshots -> {
                screenshotOverride.allow()
            }

            OnboardingRecoveryPhraseUiEvent.RevokeScreenshots -> {
                screenshotOverride.revoke()
            }

            OnboardingRecoveryPhraseUiEvent.ScreenshotTaken -> {
                savedTo.value = RecoveryPhraseSavedTo.Screenshot
            }

            OnboardingRecoveryPhraseUiEvent.Continue -> {
                navigationChannel.trySend(OnboardingRecoveryPhraseNavigationEvent.Continue)
            }
        }
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

    override fun onCleared() {
        appContext.unregisterReceiver(shareTargetChosenReceiver)
        screenshotOverride.revoke()
        File(appContext.cacheDir, SHARE_DIR).deleteRecursively()
    }
}
