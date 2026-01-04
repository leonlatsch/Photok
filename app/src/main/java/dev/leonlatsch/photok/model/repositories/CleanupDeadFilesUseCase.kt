


package dev.leonlatsch.photok.model.repositories

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.model.database.entity.LEGACY_PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CleanupDeadFilesUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    @ApplicationContext private val context: Context,
    private val encryptedStorageManager: EncryptedStorageManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    operator fun invoke() {
        scope.launch {
            val allExisting = photoRepository.findAllPhotosByImportDateDesc()

            val allFiles = context.fileList().filter {
                it.contains(LEGACY_PHOTOK_FILE_EXTENSION) || it.contains(PHOTOK_FILE_EXTENSION)
            }

            for (file in allFiles) {
                val uuid  = file.substringBefore(".")

                if (allExisting.none { uuid == it.uuid }) {
                    Timber.i("Deleting dead file: $file")
                    encryptedStorageManager.internalDeleteFile(file)
                }
            }
        }
    }
}

package dev.leonlatsch.photok.model.repositories

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.model.database.entity.LEGACY_PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CleanupDeadFilesUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    @ApplicationContext private val context: Context,
    private val encryptedStorageManager: EncryptedStorageManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    operator fun invoke() {
        scope.launch {
            val allExisting = photoRepository.findAllPhotosByImportDateDesc()

            val allFiles = context.fileList().filter {
                it.contains(LEGACY_PHOTOK_FILE_EXTENSION) || it.contains(PHOTOK_FILE_EXTENSION)
            }

            for (file in allFiles) {
                val uuid  = file.substringBefore(".")

                if (allExisting.none { uuid == it.uuid }) {
                    Timber.i("Deleting dead file: $file")
                    encryptedStorageManager.internalDeleteFile(file)
                }
            }
        }
    }
}