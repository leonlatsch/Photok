package dev.leonlatsch.photok.ui.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

/**
 * Base for all fragments that use data binding.
 *
 * @param BindingType the binding type generated when adding <data> tag to a layout.
 * @param layout the layout id with the data binding.
 * @param attachToParent attach to parent
 */
abstract class BindableFragment<BindingType : ViewDataBinding>(
    @LayoutRes private val layout: Int,
    private val attachToParent: Boolean
) : Fragment(), Bindable<BindingType> {

    /**
     * Creates layout and binding.
     * **ALWAYS** call super.onCreateView() when overwriting.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: BindingType = DataBindingUtil.inflate(inflater, layout, container, attachToParent)
        bind(binding)
        return binding.root
    }

    /**
     * Inserts the Bindings. Always call super.insertBindings() to set lifecycle owner.
     */
    override fun bind(binding: BindingType) {
        binding.lifecycleOwner = this
    }
}