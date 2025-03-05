/*
 *   Copyright 2020-2022 Leon Latsch
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

import android.app.Application
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.other.AES
import dev.leonlatsch.photok.other.AES_ALGORITHM
import dev.leonlatsch.photok.other.SHA_256
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegacyEncryptionMigrator @Inject constructor(
    private val encryptedStorageManager: EncryptedStorageManager,
    private val app: Application,
) {

    private var key: SecretKeySpec? = null
    private var iv: IvParameterSpec? = null


    fun invoke(password: String, fileName: String): Result<Unit> {
        if (key == null || iv == null) {
            key = genSecKey(password)
            iv = genLegacyIv(password)
        }

        val migrationFileName = "${fileName}_migration"

        try {
            val origInput = app.openFileInput(fileName)
            val legacyInputStream = openLegacyCipherInputStream(origInput)
            val newOutputStream = encryptedStorageManager.internalOpenFileOutput( migrationFileName )
                ?: return Result.failure(Exception("New output was null"))


            legacyInputStream.copyTo(newOutputStream)

            legacyInputStream.close()
            newOutputStream.close()

            app.deleteFile(fileName)
            encryptedStorageManager.renameFile(
                currentFileName = migrationFileName,
                newFileName = fileName,
            )

            return Result.success(Unit)
        } catch (e: Exception) {
            app.deleteFile(migrationFileName)
            return Result.failure(e)
        }
    }

    @Throws
    private fun openLegacyCipherInputStream(inputStream: InputStream): CipherInputStream {
        val cipher = Cipher.getInstance(AES_ALGORITHM).apply {
            init(Cipher.DECRYPT_MODE, key, this@LegacyEncryptionMigrator.iv)
        }

        return CipherInputStream(inputStream, cipher)
    }

    private fun genSecKey(password: String): SecretKeySpec {
        val md = MessageDigest.getInstance(SHA_256)
        val bytes = md.digest(password.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(bytes, AES)
    }

    private fun genLegacyIv(password: String): IvParameterSpec {
        val iv = ByteArray(16)
        val charArray = password.toCharArray()
        val firstChars = charArray.take(16)
        for (i in firstChars.indices) {
            iv[i] = firstChars[i].toByte()
        }

        return IvParameterSpec(iv)
    }
}
