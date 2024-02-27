/*
 *   Copyright 2020-2022 Leon Latsch
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

package dev.leonlatsch.photok.gallery.ui

import android.app.Application
import android.content.Context
import android.view.View
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.news.newfeatures.ui.FEATURE_VERSION_CODE
import dev.leonlatsch.photok.other.onMain
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Gallery.
 * Holds a Flow for the photos.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class GalleryViewModel @Inject constructor(
    app: Application,
    val photoRepository: PhotoRepository,
    private val config: Config
) : ObservableViewModel(app) {

    @get:Bindable
    var placeholderVisibility: Int = View.VISIBLE
        set(value) {
            field = value
            notifyChange(BR.placeholderVisibility, value)
        }

    @get:Bindable
    var labelsVisibility: Int = View.GONE
        set(value) {
            field = value
            notifyChange(BR.labelsVisibility, value)
        }

    val photos = Pager(
        PagingConfig(
            pageSize = PAGE_SIZE,
            maxSize = MAX_SIZE,
        )
    ) {
        photoRepository.getAllPaged()
    }.flow

    /**
     * Toggle the placeholder and label visibilities.
     */
    fun togglePlaceholder(itemCount: Int) {
        if (itemCount > 0) {
            labelsVisibility = View.VISIBLE
            placeholderVisibility = View.GONE
        } else {
            placeholderVisibility = View.VISIBLE
            labelsVisibility = View.GONE
        }
    }

    /**
     * Change the dialog string accordingly to settings.
     */
    fun getAreYouSureExportString(context: Context): String{
        return if (this.config.deleteExportedFiles) {
            context.getString(R.string.export_and_delete_are_you_sure)
        } else {
            context.getString(R.string.export_are_you_sure)
        }
    }

    /**
     * Run the [onNewsPresent] if the app was just updated.
     */
    fun runIfNews(onNewsPresent: () -> Unit) = viewModelScope.launch {
        if (config.systemLastFeatureVersionCode < FEATURE_VERSION_CODE) {
            onMain(onNewsPresent)
            config.systemLastFeatureVersionCode = FEATURE_VERSION_CODE
        }
    }

    companion object {
        private const val PAGE_SIZE = 100
        private const val MAX_SIZE = 800
    }
}