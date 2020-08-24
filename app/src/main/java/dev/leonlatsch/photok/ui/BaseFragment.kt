package dev.leonlatsch.photok.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseFragment<BindingType : ViewDataBinding>(
    @LayoutRes private val layout: Int,
    private val attachToParent: Boolean
) : Fragment() {

    /**
     * Creates layout and binding.
     * Always call super.onCreateView() when overwriting.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: BindingType = DataBindingUtil.inflate(inflater, layout, container, attachToParent)
        insertBindings(binding)
        return binding.root
    }

    /**
     * Inserts the Bindings. Always call super.insertBindings() to set lifecycle owner.
     */
    open fun insertBindings(binding: BindingType) {
        binding.lifecycleOwner = this
    }
}