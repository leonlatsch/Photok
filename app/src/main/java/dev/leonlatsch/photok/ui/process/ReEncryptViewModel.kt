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
import androidx.hilt.lifecycle.ViewModelInject
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.settings.Config
import dev.leonlatsch.photok.ui.process.base.BaseProcessViewModel
import org.mindrot.jbcrypt.BCrypt

/**
 * ViewModel for re-encrypting photos with a new password.
 * Executed after password change.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class ReEncryptViewModel @ViewModelInject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository,
    private val config: Config,
    private val encryptionManager: EncryptionManager
) : BaseProcessViewModel<Photo>() {

    lateinit var newPassword: String

    override suspend fun preProcess() {
        items = photoRepository.getAll()
        elementsToProcess = items.size
        super.preProcess()
    }

    override suspend fun processItem(item: Photo) {
        val bytes = photoRepository.readPhotoData(app, item.id!!)
        if (bytes == null) {
            failuresOccurred = true
            return
        }

        photoRepository.deletePhotoData(app, item.id)

        val result = photoRepository.writePhotoData(app, item.uuid, bytes, newPassword)
        if (!result) {
            failuresOccurred = true
        }
    }

    override suspend fun postProcess() {
        super.postProcess()
        val hashedPw = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        config.securityPassword = hashedPw
        encryptionManager.initialize(newPassword)
    }
}