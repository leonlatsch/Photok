package dev.leonlatsch.photok.security

import android.util.Log
import androidx.lifecycle.MutableLiveData
import dev.leonlatsch.photok.other.AES
import dev.leonlatsch.photok.other.SHA_256
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.spec.SecretKeySpec

class EncryptionManager {

    val encryptionKey: MutableLiveData<SecretKeySpec> = MutableLiveData()

    fun generateAndSetKey(password: String) {
        try {
            val md = MessageDigest.getInstance(SHA_256)
            val bytes = md.digest(password.toByteArray(StandardCharsets.UTF_8))
            encryptionKey.postValue(SecretKeySpec(bytes, AES))
        } catch (e: NoSuchAlgorithmException) {
            Log.e("hash error", "$e")
        }
    }

}