package dev.leonlatsch.photok.ui.setup

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.model.database.entity.Password
import dev.leonlatsch.photok.model.repositories.PasswordRepository
import dev.leonlatsch.photok.other.PASSWORD_REGEX
import dev.leonlatsch.photok.other.emptyString
import dev.leonlatsch.photok.security.EncryptionManager
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import java.util.regex.Pattern

class SetupViewModel @ViewModelInject constructor(
    private val passwordRepository: PasswordRepository,
    private val encryptionManager: EncryptionManager
) : ViewModel() {

    var passwordText: MutableLiveData<String> = MutableLiveData(emptyString())
    var confirmPasswordText: MutableLiveData<String> = MutableLiveData(emptyString())

    val setupState: MutableLiveData<SetupState> = MutableLiveData()

    var isLoading: Boolean = false

    fun savePassword() = viewModelScope.launch {
        isLoading = true
        if (validatePasswords()) {
            val bcryptHash = BCrypt.hashpw(passwordText.value, BCrypt.gensalt())
            val password = Password(bcryptHash)
            passwordRepository.insert(password)
            encryptionManager.generateAndSetKey(passwordText.value!!)
            setupState.postValue(SetupState.FINISHED)
        }
        isLoading = false
    }

    fun validatePassword(): Boolean = passwordText.value!!.isNotEmpty()
            && Pattern.matches(PASSWORD_REGEX, passwordText.value!!)

    private fun validatePasswords(): Boolean {
        return passwordText.value!!.isNotEmpty()
                && confirmPasswordText.value!!.isNotEmpty()
                && passwordText.value!! == confirmPasswordText.value!!
                && Pattern.matches(PASSWORD_REGEX, passwordText.value!!)
    }
}