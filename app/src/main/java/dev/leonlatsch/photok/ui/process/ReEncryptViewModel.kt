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

import androidx.hilt.lifecycle.ViewModelInject
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PasswordRepository
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.ui.process.base.BaseProcessViewModel

class ReEncryptViewModel @ViewModelInject constructor(
    private val photoRepository: PhotoRepository,
    private val passwordRepository: PasswordRepository
) : BaseProcessViewModel() {

    private lateinit var photos: List<Photo>
    lateinit var newPassword: String

    override suspend fun preProcess() {
        super.preProcess()
        photos = photoRepository.getAll()
    }

    override suspend fun process() {
        TODO("Not yet implemented")
    }
}