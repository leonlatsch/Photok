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

class UnlockViewModel @ViewModelInject constructor(
    private val passwordRepository: PasswordRepository,
    private val encryptionManager: EncryptionManager
) : ViewModel() {

    var passwordText: String = emptyString()
    var unlockState: MutableLiveData<UnlockState> = MutableLiveData(UnlockState.UNDEFINED)

    fun unlock() = viewModelScope.launch {
        unlockState.postValue(UnlockState.CHECKING)

        val savedPassword = passwordRepository.getPassword()
        if (BCrypt.checkpw(passwordText, savedPassword?.password)) {
            encryptionManager.initialize(passwordText)
            unlockState.postValue(UnlockState.UNLOCKED)
        } else {
            unlockState.postValue(UnlockState.LOCKED)
        }
    }

}