/*
 *   Copyright 2020 Leon Latsch
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

package dev.leonlatsch.photok.ui.process

import android.app.Application
import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.ui.backup.BackupDetails
import dev.leonlatsch.photok.ui.process.base.BaseProcessViewModel
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * ViewModel for restoring photos from a backup file.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class RestoreBackupViewModel @ViewModelInject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository,
    private val encryptionManager: EncryptionManager
) : BaseProcessViewModel<Photo>() {

    private var inputStream: ZipInputStream? = null
    private var currentEntry: ZipEntry? = null

    lateinit var zipUri: Uri
    lateinit var metaData: BackupDetails
    lateinit var origPassword: String

    override suspend fun preProcess() {
        createStream()
        currentEntry = inputStream?.nextEntry
        super.preProcess()
    }

    override suspend fun processItem(item: Photo) {
        currentEntry ?: return
        if (currentEntry!!.name == BackupDetails.FILE_NAME) {
            currentEntry = inputStream?.nextEntry
            currentEntry ?: return
        }

        val metaPhoto = findPhotoByFilename(currentEntry!!.name)
        metaPhoto ?: return

        val newPhoto = Photo(
            metaPhoto.fileName,
            System.currentTimeMillis(),
            metaPhoto.type,
            metaPhoto.size
        )

        val origBytes = readBytesFromZip()
        origBytes ?: return

        val decryptedBytes = encryptionManager.decrypt(origBytes, origPassword)
        decryptedBytes ?: return

        photoRepository.safeCreatePhoto(app, newPhoto, decryptedBytes)

        currentEntry = inputStream?.nextEntry
    }

    override suspend fun postProcess() {
        closeStream()
        super.postProcess()
    }

    private fun findPhotoByFilename(fileName: String): Photo? {
        items.forEach {
            if (it.internalFileName == fileName) {
                return it
            }
        }
        return null
    }

    private fun readBytesFromZip(): ByteArray? = inputStream?.readBytes()

    private fun closeStream() {
        inputStream?.close()
    }

    private fun createStream() {
        val input = app.contentResolver.openInputStream(zipUri)
        inputStream = if (input != null) {
            ZipInputStream(input)
        } else {
            null
        }
    }
}