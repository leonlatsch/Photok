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

    val setupState: MutableLiveData<SetupState> = MutableLiveData(SetupState.SETUP)

    fun savePassword() = viewModelScope.launch {
        setupState.postValue(SetupState.LOADING)

        if (validateBothPasswords()) {
            val bcryptHash = BCrypt.hashpw(passwordText.value, BCrypt.gensalt())
            val password = Password(bcryptHash)
            passwordRepository.insert(password)
            encryptionManager.initialize(passwordText.value!!)
            setupState.postValue(SetupState.FINISHED)
        } else {
            setupState.postValue(SetupState.SETUP)
        }
    }

    fun validatePassword(): Boolean = passwordText.value!!.isNotEmpty()
            && Pattern.matches(PASSWORD_REGEX, passwordText.value!!)

    fun passwordsEqual(): Boolean = passwordText.value == confirmPasswordText.value

    fun validateBothPasswords(): Boolean = passwordText.value!!.isNotEmpty()
            && confirmPasswordText.value!!.isNotEmpty()
            && validatePassword()
            && passwordsEqual()
}