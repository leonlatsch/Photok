package dev.leonlatsch.photok.ui.components

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * Abstract class for Activities with binding context.
 * Inflates layout file and sets binding of type [BindingType].
 *
 * @since 1.0.0
 */
abstract class BindableActivity<BindingType : ViewDataBinding>(
    @LayoutRes private val layout: Int
) : BaseActivity(), Bindable<BindingType> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: BindingType = DataBindingUtil.setContentView(this, layout)
        bind(binding)
    }

    override fun bind(binding: BindingType) {
    }
}