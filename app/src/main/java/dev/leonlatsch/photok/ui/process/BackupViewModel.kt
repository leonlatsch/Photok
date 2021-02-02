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
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.settings.Config
import dev.leonlatsch.photok.ui.backup.BackupMetaData
import dev.leonlatsch.photok.ui.process.base.BaseProcessViewModel
import timber.log.Timber
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/**
 * ViewModel to create a backup.
 * Backups photos and meta data to zip file.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class BackupViewModel @Inject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository,
    private val config: Config
) : BaseProcessViewModel<Photo>(app) {

    lateinit var uri: Uri
    lateinit var outputStream: ZipOutputStream
    private var backedUpPhotos = arrayListOf<Photo>()
    private val gson: Gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

    override suspend fun preProcess() {
        items = photoRepository.getAll()
        elementsToProcess = items.size
        openZipFile()
        super.preProcess()
    }

    override suspend fun processItem(item: Photo) {
        val rawData = photoRepository.readRawPhotoFileFromInternal(app, item)
        if (rawData == null) {
            failuresOccurred = true
            return
        }

        val success = writeZipEntry(item.internalFileName, rawData)
        if (success) {
            backedUpPhotos.add(item)
        } else {
            failuresOccurred = true
        }
    }

    override suspend fun postProcess() {
        val details = BackupMetaData(config.securityPassword!!, backedUpPhotos)
        val jsonString = gson.toJson(details)
        writeZipEntry(BackupMetaData.FILE_NAME, jsonString.toByteArray())

        closeZipFile()
        super.postProcess()
    }

    private fun openZipFile() {
        val out = app.contentResolver.openOutputStream(uri)
        outputStream = ZipOutputStream(out)

    }

    private fun writeZipEntry(fileName: String, data: ByteArray): Boolean {
        return try {
            val entry = ZipEntry(fileName)
            outputStream.putNextEntry(entry)
            outputStream.write(data)
            outputStream.closeEntry()
            true
        } catch (e: IOException) {
            Timber.d("Cloud not write to backup: $e")
            false
        }
    }

    private fun closeZipFile() {
        outputStream.close()
    }
}
