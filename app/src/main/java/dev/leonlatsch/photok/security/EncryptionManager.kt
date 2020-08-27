package dev.leonlatsch.photok.security

import android.util.Log
import dev.leonlatsch.photok.other.AES
import dev.leonlatsch.photok.other.AES_ALGORITHM
import dev.leonlatsch.photok.other.SHA_256
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class EncryptionManager {

    /**
     * DO NOT SET VALUE FROM EXTERNAL!
     * Instead use #initialize()
     */
    private var encryptionKey: SecretKeySpec? = null
    private var encryptionCipher: Cipher = Cipher.getInstance(AES_ALGORITHM)
    private var decryptionCipher: Cipher = Cipher.getInstance(AES_ALGORITHM)

    var isReady: Boolean = false

    fun initialize(password: String) {
        try {
            val md = MessageDigest.getInstance(SHA_256)
            val bytes = md.digest(password.toByteArray(StandardCharsets.UTF_8))
            encryptionKey = SecretKeySpec(bytes, AES)
            initCiphers()
        } catch (e: NoSuchAlgorithmException) {
            Log.e(EncryptionManager::class.toString(), "Error initializing EncryptionManager: $e")
            isReady = false
        }
    }

    private fun initCiphers() {
        encryptionCipher.init(Cipher.ENCRYPT_MODE, encryptionKey)
        decryptionCipher.init(Cipher.DECRYPT_MODE, encryptionKey)
    }

    fun encrypt(bytes: ByteArray): ByteArray? {
        return try {
            encryptionCipher.doFinal(bytes)
        } catch (e: GeneralSecurityException) {
            Log.e(EncryptionManager::class.toString(), "Error encrypting image: $e")
            null
        }
    }

    fun decrypt(encryptedBytes: ByteArray): ByteArray? {
        return try {
            decryptionCipher.doFinal(encryptedBytes)
        } catch (e: GeneralSecurityException) {
            Log.e(EncryptionManager::class.toString(), "Error decryption image: $e")
            null
        }
    }
}