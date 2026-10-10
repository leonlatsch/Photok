/*
 *   Copyright 2020–2026 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.leonlatsch.photok.gallery.components

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.pro.purchases.PurchaseService
import dev.leonlatsch.photok.settings.data.Config
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed interface ProGalleryBannerUiEvent {
    data class OnDismiss(val context: Context) : ProGalleryBannerUiEvent
}

@HiltViewModel
class ProGalleryBannerViewModel @Inject constructor(
    private val config: Config,
    purchaseService: PurchaseService,
    photoRepository: PhotoRepository,
) : ViewModel() {

    val uiState: StateFlow<Boolean> = combine(
        config.valuesFlow,
        purchaseService.observe(),
        photoRepository.observeCount(),
    ) { _, proActive, photoCount ->
        val installDate = config.systemInstallDate
        val hasInstalledForAWeek = installDate != null &&
                System.currentTimeMillis() - installDate >= TimeUnit.DAYS.toMillis(7)

        BuildConfig.PLAY &&
                !proActive &&
                !config.proBannerDismissed &&
                photoCount > 0 &&
                hasInstalledForAWeek
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun handleUiEvent(event: ProGalleryBannerUiEvent) {
        when (event) {
            is ProGalleryBannerUiEvent.OnDismiss -> {
                config.proBannerDismissed = true

                val dismissedString = event.context.getString(R.string.gallery_pro_banner_dismissed)
                Toast.makeText(event.context, dismissedString, Toast.LENGTH_LONG).show()
            }
        }
    }
}
