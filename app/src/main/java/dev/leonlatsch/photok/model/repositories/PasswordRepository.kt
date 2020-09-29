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

package dev.leonlatsch.photok.model.repositories

import dev.leonlatsch.photok.model.database.dao.PasswordDao
import dev.leonlatsch.photok.model.database.entity.Password
import javax.inject.Inject

/**
 * Repository for [Password].
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class PasswordRepository @Inject constructor(
    private val passwordDao: PasswordDao
) {
    suspend fun insert(password: Password) = passwordDao.insert(password)

    suspend fun delete(password: Password) = passwordDao.delete(password)

    suspend fun update(password: Password) = passwordDao.update(password)

    suspend fun getPassword() = passwordDao.getPassword()
}