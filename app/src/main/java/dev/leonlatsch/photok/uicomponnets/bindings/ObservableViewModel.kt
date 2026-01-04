


package dev.leonlatsch.photok.uicomponnets.bindings

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import timber.log.Timber

/**
 * Base for all ViewModels. Implements [Observable].
 *
 * @sample dev.leonlatsch.photok.main.backup.RestoreBackupViewModel.metaData
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class ObservableViewModel(app: Application) : AndroidViewModel(app), Observable {

    private val changeListeners: PropertyChangeRegistry = PropertyChangeRegistry()
    private val valueChangeRegistry: PropertyValueChangeRegistry = PropertyValueChangeRegistry()

    override fun addOnPropertyChangedCallback(listener: OnPropertyChangedCallback) {
        changeListeners.add(listener)
    }

    override fun removeOnPropertyChangedCallback(listener: OnPropertyChangedCallback) {
        changeListeners.remove(listener)
    }

    /**
     * Add callback to [valueChangeRegistry]
     */
    fun <T> addOnPropertyValueChangedCallback(callback: PropertyChangedValueCallback) =
        valueChangeRegistry.addValueCallback(callback)

    /**
     * Remove callback from [valueChangeRegistry]
     */
    fun removeOnPropertyValueChangedCallback(callback: PropertyChangedValueCallback) =
        valueChangeRegistry.removeValueCallback(callback)

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
     * Notify changes in instances of [PropertyChangedValueCallback].
     */
    fun notifyChange(property: Int, newValue: Any?) {
        changeListeners.notifyCallbacks(this, property, null)
        valueChangeRegistry.notifyCallbacks(property, newValue)
    }

    /**
     * Handy version of [addOnPropertyChangedCallback].
     * Takes a [block] that gets posted to MainLooper.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> addOnPropertyChange(propertyId: Int, block: (newValue: T) -> Unit) {
        valueChangeRegistry.addValueCallback(object : PropertyChangedValueCallback {
            override fun onCallback(property: Int, newValue: Any?) {
                if (property == propertyId) {
                    Handler(Looper.getMainLooper()).post {
                        try {
                            block(newValue as T)
                        } catch (e: ClassCastException) {
                            Timber.d("newValue is not type of T")
                        }
                    }
                }
            }
        })
    }

    /**
     * Used for setting up the viewModel. Can be overridden.
     */
    open fun setup() {
    }
}

package dev.leonlatsch.photok.uicomponnets.bindings

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import timber.log.Timber

/**
 * Base for all ViewModels. Implements [Observable].
 *
 * @sample dev.leonlatsch.photok.main.backup.RestoreBackupViewModel.metaData
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class ObservableViewModel(app: Application) : AndroidViewModel(app), Observable {

    private val changeListeners: PropertyChangeRegistry = PropertyChangeRegistry()
    private val valueChangeRegistry: PropertyValueChangeRegistry = PropertyValueChangeRegistry()

    override fun addOnPropertyChangedCallback(listener: OnPropertyChangedCallback) {
        changeListeners.add(listener)
    }

    override fun removeOnPropertyChangedCallback(listener: OnPropertyChangedCallback) {
        changeListeners.remove(listener)
    }

    /**
     * Add callback to [valueChangeRegistry]
     */
    fun <T> addOnPropertyValueChangedCallback(callback: PropertyChangedValueCallback) =
        valueChangeRegistry.addValueCallback(callback)

    /**
     * Remove callback from [valueChangeRegistry]
     */
    fun removeOnPropertyValueChangedCallback(callback: PropertyChangedValueCallback) =
        valueChangeRegistry.removeValueCallback(callback)

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
     * Notify changes in instances of [PropertyChangedValueCallback].
     */
    fun notifyChange(property: Int, newValue: Any?) {
        changeListeners.notifyCallbacks(this, property, null)
        valueChangeRegistry.notifyCallbacks(property, newValue)
    }

    /**
     * Handy version of [addOnPropertyChangedCallback].
     * Takes a [block] that gets posted to MainLooper.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> addOnPropertyChange(propertyId: Int, block: (newValue: T) -> Unit) {
        valueChangeRegistry.addValueCallback(object : PropertyChangedValueCallback {
            override fun onCallback(property: Int, newValue: Any?) {
                if (property == propertyId) {
                    Handler(Looper.getMainLooper()).post {
                        try {
                            block(newValue as T)
                        } catch (e: ClassCastException) {
                            Timber.d("newValue is not type of T")
                        }
                    }
                }
            }
        })
    }

    /**
     * Used for setting up the viewModel. Can be overridden.
     */
    open fun setup() {
    }
}