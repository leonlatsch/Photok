/*
 *   Copyright 2020-2021 Leon Latsch
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

package dev.leonlatsch.photok.gallery.ui.collections

import android.app.Application
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.lifecycle.SingleLiveEvent
import dev.leonlatsch.photok.model.database.entity.Collection
import dev.leonlatsch.photok.model.repositories.CollectionRepository
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.other.onMain
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for adding a collection
 *
 * @since 2.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class AddCollectionViewModel @Inject constructor(
    app: Application,
    private val collectionRepository: CollectionRepository
) : ObservableViewModel(app) {

    val navigationEvent = SingleLiveEvent<AddCollectionNavigationEvent>()

    @Bindable
    var collectionName: String = String.empty
        set(value) {
            field = value
            notifyChange(BR.collectionName, value)
        }

    fun addCollection() = viewModelScope.launch(Dispatchers.IO) {
        val collection = Collection(collectionName, "") // TODO: handle cover
        collectionRepository.insert(collection)

        onMain {
            navigationEvent.value = AddCollectionNavigationEvent.Dismiss
        }
    }
}