package dev.leonlatsch.photok.ui.start

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.model.repositories.PasswordRepository
import kotlinx.coroutines.launch

class SplashScreenViewModel @ViewModelInject constructor(
    private val passwordRepository: PasswordRepository
) : ViewModel() {

    var vaultState: MutableLiveData<VaultState> = MutableLiveData()

    fun checkVaultState() = viewModelScope.launch {

        val password = passwordRepository.getPassword()?.password
        if (password == null) {
            vaultState.postValue(VaultState.SETUP)
        }
    }
}