package dev.leonlatsch.photok.ui.unlock

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.model.repositories.PasswordRepository
import dev.leonlatsch.photok.other.emptyString
import dev.leonlatsch.photok.security.EncryptionManager
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

class LockedViewModel @ViewModelInject constructor(
    private val passwordRepository: PasswordRepository,
    private val encryptionManager: EncryptionManager
) : ViewModel() {

    var passwordText = emptyString()
    var unlockState: MutableLiveData<UnlockState> = MutableLiveData()

    fun unlock() = viewModelScope.launch {
        val savedPassword = passwordRepository.getPassword()
        if (BCrypt.checkpw(passwordText, savedPassword?.password)) {
            encryptionManager.generateAndSetKey(passwordText)
            unlockState.postValue(UnlockState.UNLOCKED)
        } else {
            unlockState.postValue(UnlockState.LOCKED)
        }
    }

}