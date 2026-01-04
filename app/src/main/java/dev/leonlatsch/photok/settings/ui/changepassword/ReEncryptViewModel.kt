


package dev.leonlatsch.photok.settings.ui.changepassword

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.security.PasswordManager
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessViewModel
import javax.inject.Inject

/**
 * ViewModel for re-encrypting photos with a new password.
 * Executed after password change.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class ReEncryptViewModel @Inject constructor(
    app: Application,
    private val photoRepository: PhotoRepository,
    private val encryptionManager: EncryptionManager,
    private val encryptedStorageManager: EncryptedStorageManager,
    private val passwordManager: PasswordManager,
) : BaseProcessViewModel<Photo>(app) {

    lateinit var oldPassword: String
    lateinit var newPassword: String

    override suspend fun preProcess() {
        items = photoRepository.findAllPhotosByImportDateDesc()
        elementsToProcess = items.size

        passwordManager.storePassword(newPassword)
        encryptionManager.initialize(newPassword)

        encryptionManager.keyCacheEnabled = true

        super.preProcess()
    }

    override suspend fun processItem(item: Photo) {
        val fileSuccess = encryptedStorageManager.reEncryptFile(
            fileName = item.internalFileName,
            oldPassword = oldPassword,
        )
        val thumbnailSuccess =
            encryptedStorageManager.reEncryptFile(
                fileName = item.internalThumbnailFileName,
                oldPassword = oldPassword,
            )

        val videoPreviewSuccess = if (item.type.isVideo) {
            encryptedStorageManager.reEncryptFile(
                fileName = item.internalVideoPreviewFileName,
                oldPassword = oldPassword,
            )
        } else {
            true // Just set true, since it can be ignored
        }

        if (!fileSuccess || !thumbnailSuccess || !videoPreviewSuccess) {
            failuresOccurred()
            return
        }
    }

    override suspend fun postProcess() {
        super.postProcess()
        encryptionManager.keyCacheEnabled = false
    }
}

package dev.leonlatsch.photok.settings.ui.changepassword

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.security.PasswordManager
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessViewModel
import javax.inject.Inject

/**
 * ViewModel for re-encrypting photos with a new password.
 * Executed after password change.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class ReEncryptViewModel @Inject constructor(
    app: Application,
    private val photoRepository: PhotoRepository,
    private val encryptionManager: EncryptionManager,
    private val encryptedStorageManager: EncryptedStorageManager,
    private val passwordManager: PasswordManager,
) : BaseProcessViewModel<Photo>(app) {

    lateinit var oldPassword: String
    lateinit var newPassword: String

    override suspend fun preProcess() {
        items = photoRepository.findAllPhotosByImportDateDesc()
        elementsToProcess = items.size

        passwordManager.storePassword(newPassword)
        encryptionManager.initialize(newPassword)

        encryptionManager.keyCacheEnabled = true

        super.preProcess()
    }

    override suspend fun processItem(item: Photo) {
        val fileSuccess = encryptedStorageManager.reEncryptFile(
            fileName = item.internalFileName,
            oldPassword = oldPassword,
        )
        val thumbnailSuccess =
            encryptedStorageManager.reEncryptFile(
                fileName = item.internalThumbnailFileName,
                oldPassword = oldPassword,
            )

        val videoPreviewSuccess = if (item.type.isVideo) {
            encryptedStorageManager.reEncryptFile(
                fileName = item.internalVideoPreviewFileName,
                oldPassword = oldPassword,
            )
        } else {
            true // Just set true, since it can be ignored
        }

        if (!fileSuccess || !thumbnailSuccess || !videoPreviewSuccess) {
            failuresOccurred()
            return
        }
    }

    override suspend fun postProcess() {
        super.postProcess()
        encryptionManager.keyCacheEnabled = false
    }
}