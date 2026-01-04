package dev.leonlatsch.photok.security

import java.io.InputStream
import java.io.OutputStream
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey

interface EncryptionManager {
    val isReady: Boolean
    var keyCacheEnabled: Boolean
    fun initialize(password: String): Result<Unit>
    fun initialize(key: SecretKey): Result<Unit>
    fun reset()
    fun getKeyOrNull(): SecretKey?

    fun createCipherInputStream(
        input: InputStream,
        password: String? = null,
    ): CipherInputStream?
    fun createCipherOutputStream(
        output: OutputStream,
        password: String? = null,
    ): CipherOutputStream?
}