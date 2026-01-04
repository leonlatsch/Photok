


package dev.leonlatsch.photok.uicomponnets.bindings

/**
 * Listener Registry for [PropertyChangedValueCallback]
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class PropertyValueChangeRegistry {

    private val callbacks: ArrayList<PropertyChangedValueCallback> = arrayListOf()

    /**
     * Add a [PropertyChangedValueCallback] to the registry.
     */
    fun addValueCallback(callback: PropertyChangedValueCallback) = callbacks.add(callback)

    /**
     * Remove a [PropertyChangedValueCallback] from the registry.
     */
    fun removeValueCallback(callback: PropertyChangedValueCallback) = callbacks.remove(callback)

    /**
     * Notify all callbacks.
     */
    fun notifyCallbacks(property: Int, newValue: Any?) {
        callbacks.forEach {
            it.onCallback(property, newValue)
        }
    }
}

package dev.leonlatsch.photok.uicomponnets.bindings

/**
 * Listener Registry for [PropertyChangedValueCallback]
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class PropertyValueChangeRegistry {

    private val callbacks: ArrayList<PropertyChangedValueCallback> = arrayListOf()

    /**
     * Add a [PropertyChangedValueCallback] to the registry.
     */
    fun addValueCallback(callback: PropertyChangedValueCallback) = callbacks.add(callback)

    /**
     * Remove a [PropertyChangedValueCallback] from the registry.
     */
    fun removeValueCallback(callback: PropertyChangedValueCallback) = callbacks.remove(callback)

    /**
     * Notify all callbacks.
     */
    fun notifyCallbacks(property: Int, newValue: Any?) {
        callbacks.forEach {
            it.onCallback(property, newValue)
        }
    }
}