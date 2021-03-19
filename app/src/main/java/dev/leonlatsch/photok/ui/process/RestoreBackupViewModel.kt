/*
 *   Copyright 2020-2021 Leon Latsch
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
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.lazyClose
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.ui.backup.BackupMetaData
import dev.leonlatsch.photok.ui.process.base.BaseProcessViewModel
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject

/**
 * ViewModel for restoring photos from a backup file.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class RestoreBackupViewModel @Inject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository,
    private val encryptionManager: EncryptionManager,
    private val encryptedStorageManager: EncryptedStorageManager
) : BaseProcessViewModel<Photo>(app) {

    private var zipInputStream: ZipInputStream? = null
    private var currentEntry: ZipEntry? = null

    lateinit var zipUri: Uri
    lateinit var metaData: BackupMetaData
    lateinit var origPassword: String

    override suspend fun preProcess() {
        createStream()
        zipInputStream ?: cancel()
        nextEntry()
        super.preProcess()
    }

    override suspend fun processItem(item: Photo) {
        currentEntry ?: return
        skipIfMetaEntry()
        currentEntry ?: return

        copyEntryToInternalStorage()

        nextEntry()
        currentEntry ?: return

        copyEntryToInternalStorage()

        nextEntry()
    }

    private fun skipIfMetaEntry() {
        currentEntry ?: return
        if (currentEntry!!.name == BackupMetaData.FILE_NAME) {
            nextEntry()
        }
    }

    private fun copyEntryToInternalStorage() {
        val zipEntryInputStream =
            encryptionManager.createCipherInputStream(zipInputStream!!, origPassword)
        val encryptedOutputStream =
            encryptedStorageManager.internalOpenEncryptedFileOutput(app, currentEntry!!.name)

        zipEntryInputStream ?: return
        encryptedOutputStream ?: return

        zipEntryInputStream.copyTo(encryptedOutputStream)
        encryptedOutputStream.lazyClose()
    }

    private suspend fun insertMeta() {
        metaData.photos.forEach {
            val newPhoto = Photo(
                it.fileName,
                System.currentTimeMillis(),
                it.type,
                it.size,
                it.uuid
            )
            photoRepository.insert(newPhoto)
        }
    }

    override suspend fun postProcess() {
        zipInputStream?.lazyClose()
        insertMeta()
        super.postProcess()
    }

    private fun nextEntry() {
        currentEntry = zipInputStream?.nextEntry
    }

    private fun findPhoto(fileName: String): Photo? {
        items.forEach {
            if (it.internalFileName == fileName) {
                return it
            }
        }
        return null
    }

    private fun createStream() {
        val input = app.contentResolver.openInputStream(zipUri)
        zipInputStream = ZipInputStream(input)
    }
}