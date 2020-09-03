package dev.leonlatsch.photok.security

import android.util.Log
import dev.leonlatsch.photok.other.AES
import dev.leonlatsch.photok.other.AES_ALGORITHM
import dev.leonlatsch.photok.other.SHA_256
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manages encryption.
 * Holds the AES key and serves it to the app's components.
 *
 * @since 1.0.0
 */
class EncryptionManager {

    private var encryptionKey: SecretKeySpec? = null
    private var ivParameterSpec: IvParameterSpec? = null

    var isReady: Boolean = false

    /**
     * Initialize the [SecretKeySpec] with a [password].
     * Uses the [password] to create a SHA-256 hash binary that is used to create [SecretKeySpec].
     * Generates [IvParameterSpec] for GCM.
     *
     * @param password the password string to use.
     */
    fun initialize(password: String) {
        try {
            val md = MessageDigest.getInstance(SHA_256)
            val bytes = md.digest(password.toByteArray(StandardCharsets.UTF_8))
            encryptionKey = SecretKeySpec(bytes, AES)
            ivParameterSpec = genIv(password)
            isReady = true
        } catch (e: GeneralSecurityException) {
            Log.e(EncryptionManager::class.toString(), "Error initializing EncryptionManager: $e")
            isReady = false
        }
    }

    private fun genIv(password: String): IvParameterSpec {
        val iv = ByteArray(16)
        val charArray = password.toCharArray()
        for (i in charArray.indices){
            iv[i] = charArray[i].toByte()
        }
        return IvParameterSpec(iv)
    }

    /**
     * Encrypt a [ByteArray] with the stored [SecretKeySpec].
     */
    fun encrypt(bytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(AES_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, ivParameterSpec)
        return cipher.doFinal(bytes)
    }

    /**
     * Decrypt a [ByteArray] with the stored [SecretKeySpec]
     */
    fun decrypt(encryptedBytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(AES_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, ivParameterSpec)
        return cipher.doFinal(encryptedBytes)
    }
}