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

class EncryptionManager private constructor() {

    // TODO: Inject to Converters with Hilt
    companion object {
        val instance: EncryptionManager = EncryptionManager()
    }

    private var encryptionKey: SecretKeySpec? = null
    private var encryptionCipher: Cipher = Cipher.getInstance(AES_ALGORITHM)
    private var decryptionCipher: Cipher = Cipher.getInstance(AES_ALGORITHM)

    var isReady: Boolean = false

    fun initialize(password: String) {
        try {
            val md = MessageDigest.getInstance(SHA_256)
            val bytes = md.digest(password.toByteArray(StandardCharsets.UTF_8))
            encryptionKey = SecretKeySpec(bytes, AES)
            initCiphers(password)
        } catch (e: GeneralSecurityException) {
            Log.e(EncryptionManager::class.toString(), "Error initializing EncryptionManager: $e")
            isReady = false
        }
    }

    private fun initCiphers(password: String) {
        val ivParameterSpec = genIv(password)
        encryptionCipher.init(Cipher.ENCRYPT_MODE, encryptionKey, ivParameterSpec)
        decryptionCipher.init(Cipher.DECRYPT_MODE, encryptionKey, ivParameterSpec)
    }

    private fun genIv(password: String): IvParameterSpec {
        val iv = ByteArray(16)
        val charArray = password.toCharArray()
        for (i in charArray.indices){
            iv[i] = charArray[i].toByte()
        }
        return IvParameterSpec(iv)
    }

    fun encrypt(bytes: ByteArray): ByteArray = encryptionCipher.doFinal(bytes)

    fun decrypt(encryptedBytes: ByteArray): ByteArray = decryptionCipher.doFinal(encryptedBytes)
}