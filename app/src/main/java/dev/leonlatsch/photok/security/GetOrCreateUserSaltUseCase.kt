/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.security

import dev.leonlatsch.photok.settings.data.Config
import java.security.SecureRandom
import javax.inject.Inject
import kotlin.io.encoding.Base64

class GetOrCreateUserSaltUseCase @Inject constructor(
    private val config: Config
) {
    operator fun invoke(): ByteArray {
        val storedSalt = config.userSalt

        return if (storedSalt != null) {
            Base64.Default.decode(storedSalt)
        } else {
            val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
            config.userSalt = Base64.Default.encode(salt)
            salt
        }
    }
}