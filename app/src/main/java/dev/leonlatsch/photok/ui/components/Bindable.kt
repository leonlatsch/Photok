package dev.leonlatsch.photok.ui.components

import androidx.databinding.ViewDataBinding

/**
 * Interface for all bindable base ui components.
 *
 * @since 1.0.0
 */
interface Bindable<BindingType : ViewDataBinding> {

    /**
     * Used to insert bindings.
     */
    fun bind(binding: BindingType)

}