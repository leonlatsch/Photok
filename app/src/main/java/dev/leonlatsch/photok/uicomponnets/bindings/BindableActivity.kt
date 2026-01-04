


package dev.leonlatsch.photok.uicomponnets.bindings

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import dev.leonlatsch.photok.uicomponnets.base.BaseActivity

/**
 * Abstract class for Activities with binding context.
 * Inflates layout file and sets binding of type [BindingType].
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class BindableActivity<BindingType : ViewDataBinding>(
    @LayoutRes private val layout: Int
) : BaseActivity(), Bindable<BindingType> {

    final override lateinit var binding: BindingType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, layout)
        bind(binding)
    }

    override fun bind(binding: BindingType) {
        binding.lifecycleOwner = this
    }
}

package dev.leonlatsch.photok.uicomponnets.bindings

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import dev.leonlatsch.photok.uicomponnets.base.BaseActivity

/**
 * Abstract class for Activities with binding context.
 * Inflates layout file and sets binding of type [BindingType].
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class BindableActivity<BindingType : ViewDataBinding>(
    @LayoutRes private val layout: Int
) : BaseActivity(), Bindable<BindingType> {

    final override lateinit var binding: BindingType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, layout)
        bind(binding)
    }

    override fun bind(binding: BindingType) {
        binding.lifecycleOwner = this
    }
}