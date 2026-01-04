package dev.leonlatsch.photok.uicomponnets.bindings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import dev.leonlatsch.photok.uicomponnets.base.BaseFragment

/**
 * Base for all fragments that use data binding.
 *
 * @param BindingType the binding type generated when adding <data> tag to a layout.
 * @param layout the layout id with the data binding.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class BindableFragment<BindingType : ViewDataBinding>(
    @LayoutRes private val layout: Int
) : BaseFragment(), Bindable<BindingType> {

    final override lateinit var binding: BindingType

    /**
     * Creates layout and binding.
     * **ALWAYS** call super.onCreateView() when overwriting.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layout, container, false)
        bind(binding)
        return binding.root
    }

    /**
     * When called, this fragment will call setup() on its viewModel
     */
    fun useViewModel(viewModel: ObservableViewModel) {
        viewModel.setup()
    }

    /**
     * Inserts the Bindings. Always call super.insertBindings() to set lifecycle owner.
     */
    override fun bind(binding: BindingType) {
        binding.lifecycleOwner = viewLifecycleOwner
    }
}