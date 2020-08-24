package dev.leonlatsch.photok.ui.setup

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.model.database.entity.Password
import dev.leonlatsch.photok.model.repositories.PasswordRepository
import dev.leonlatsch.photok.security.EncryptionManager
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

class SetupViewModel @ViewModelInject constructor(
    private val passwordRepository: PasswordRepository,
    private val encryptionManager: EncryptionManager
) : ViewModel() {

    fun savePassword(passwordText: String) = viewModelScope.launch {
        val bcryptHash = BCrypt.hashpw(passwordText, BCrypt.gensalt())
        val password = Password(bcryptHash)
        passwordRepository.insert(password)
        encryptionManager.generateAndSetKey(passwordText)
    }
}