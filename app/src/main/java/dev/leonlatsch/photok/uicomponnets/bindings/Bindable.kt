


package dev.leonlatsch.photok.uicomponnets.bindings

import androidx.databinding.ViewDataBinding

/**
 * Interface for all bindable base ui components.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
interface Bindable<BindingType : ViewDataBinding> {


    /**
     * Binding instance. Contains all Views and Variables in a layout.
     */
    var binding: BindingType

    /**
     * Used to insert bindings.
     * @sample dev.leonlatsch.photok.main.components.bindings.BindableActivity.bind
     */
    fun bind(binding: BindingType)

}

package dev.leonlatsch.photok.uicomponnets.bindings

import androidx.databinding.ViewDataBinding

/**
 * Interface for all bindable base ui components.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
interface Bindable<BindingType : ViewDataBinding> {


    /**
     * Binding instance. Contains all Views and Variables in a layout.
     */
    var binding: BindingType

    /**
     * Used to insert bindings.
     * @sample dev.leonlatsch.photok.main.components.bindings.BindableActivity.bind
     */
    fun bind(binding: BindingType)

}