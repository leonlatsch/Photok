package dev.leonlatsch.photok.ui.locked

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import dev.leonlatsch.photok.model.repositories.PasswordRepository
import dev.leonlatsch.photok.other.emptyString

class LockedViewModel @ViewModelInject constructor(
    private val passwordRepository: PasswordRepository
) : ViewModel() {

    var passwordText = emptyString()

    fun unlock() {

    }

}