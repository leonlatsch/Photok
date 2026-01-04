


package dev.leonlatsch.photok.uicomponnets.bindings

/**
 * Custom Callback for sending newValue to listeners in code.
 * Operates separated from UI Callbacks.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
interface PropertyChangedValueCallback {

    /**
     * Called when data changes.
     */
    fun onCallback(property: Int, newValue: Any?)
}

package dev.leonlatsch.photok.uicomponnets.bindings

/**
 * Custom Callback for sending newValue to listeners in code.
 * Operates separated from UI Callbacks.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
interface PropertyChangedValueCallback {

    /**
     * Called when data changes.
     */
    fun onCallback(property: Int, newValue: Any?)
}