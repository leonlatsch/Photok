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