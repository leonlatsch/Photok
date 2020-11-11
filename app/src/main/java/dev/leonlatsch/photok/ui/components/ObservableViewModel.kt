/*
 *   Copyright 2020 Leon Latsch
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

package dev.leonlatsch.photok.ui.components

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel

/**
 * Base for all ViewModels. Implements [Observable].
 *
 * @sample dev.leonlatsch.photok.ui.backup.ValidateBackupViewModel.metaData
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class ObservableViewModel(app: Application) : AndroidViewModel(app), Observable {

    private val changeListeners: PropertyChangeRegistry = PropertyChangeRegistry()

    override fun addOnPropertyChangedCallback(listener: Observable.OnPropertyChangedCallback) {
        changeListeners.add(listener)
    }

    override fun removeOnPropertyChangedCallback(listener: Observable.OnPropertyChangedCallback) {
        changeListeners.remove(listener)
    }

    /**
     * Notify changes on all properties.
     */
    fun notifyChange() {
        changeListeners.notifyCallbacks(this, 0, null)
    }

    /**
     * Notify changes in [view].
     */
    fun notifyChange(view: Int) {
        changeListeners.notifyCallbacks(this, view, null)
    }

    /**
     * Handy version of [addOnPropertyChangedCallback].
     * Takes a [block] that gets posted to MainLooper.
     */
    fun addOnPropertyChange(property: Int, block: () -> Unit) {
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (propertyId == property) {
                    Handler(Looper.getMainLooper()).post(block)
                }
            }
        })
    }
}