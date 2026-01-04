package dev.leonlatsch.photok.recoverymenu

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.other.SingleLiveEvent
import dev.leonlatsch.photok.settings.ui.hideapp.usecase.ToggleMainComponentUseCase
import javax.inject.Inject

@HiltViewModel
class RecoveryMenuViewModel @Inject constructor(
    private val toggleMainComponentUseCase: ToggleMainComponentUseCase
) : ViewModel() {

    val navigationEvent = SingleLiveEvent<RecoveryMenuNavigator.NavigationEvent>()

    fun openPhotok() {
        navigationEvent.value = RecoveryMenuNavigator.NavigationEvent.OpenPhotok
    }

    fun resetHidePhotoSetting() {
        toggleMainComponentUseCase()

        navigationEvent.value = RecoveryMenuNavigator.NavigationEvent.AfterResetHideApp
    }
}