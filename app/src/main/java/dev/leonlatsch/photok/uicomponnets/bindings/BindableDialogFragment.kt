package dev.leonlatsch.photok.uicomponnets.bindings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import dev.leonlatsch.photok.uicomponnets.base.BaseDialogFragment

/**
 * Base for all Dialogs that use Bindings.
 *
 * @param BindingType The type of the generated binding
 * @param layout The layout resource id
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class BindableDialogFragment<BindingType : ViewDataBinding>(
    @LayoutRes private val layout: Int
) : BaseDialogFragment(), Bindable<BindingType> {

    final override lateinit var binding: BindingType

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

    override fun bind(binding: BindingType) {
        binding.lifecycleOwner = viewLifecycleOwner
    }
}