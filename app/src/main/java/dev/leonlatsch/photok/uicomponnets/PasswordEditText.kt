/*
 *   Copyright 2020-2021 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.leonlatsch.photok.uicomponnets

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnClickListener
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.databinding.*
import androidx.databinding.adapters.ListenerUtil
import androidx.databinding.adapters.TextViewBindingAdapter
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.PasswordEditTextBinding
import dev.leonlatsch.photok.uicomponnets.bindings.Bindable

/**
 * Custom Edit Text for Passwords.
 * Handles password visibility.
 * Should be used in all password layouts.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@InverseBindingMethods(
    value = [
        InverseBindingMethod(
            type = PasswordEditText::class,
            attribute = "textValue",
            event = "android:textAttrChanged",
            method = "getTextValue"
        )]
)
class PasswordEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle), Bindable<PasswordEditTextBinding> {

    override lateinit var binding: PasswordEditTextBinding

    private val onShowPasswordClickListener = OnClickListener {
        val origTf = binding.passwordEditTextValue.typeface
        binding.passwordEditTextValue.inputType = when (binding.passwordEditTextValue.inputType) {
            INPUT_TYPE_PASSWORD -> {
                binding.passwordEditTextIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_eye_closed
                    )
                )
                INPUT_TYPE_TEXT
            }
            INPUT_TYPE_TEXT -> {
                binding.passwordEditTextIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_eye
                    )
                )
                INPUT_TYPE_PASSWORD
            }
            else -> {
                binding.passwordEditTextIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_eye
                    )
                )
                INPUT_TYPE_PASSWORD
            }
        }
        binding.passwordEditTextValue.typeface = origTf
    }

    init {
        if (isInEditMode) {
            LayoutInflater.from(context).inflate(R.layout.password_edit_text, this, true)
        } else {
            binding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.password_edit_text,
                this,
                true
            )
            bind(binding)
            binding.root
        }

        attrs?.let {
            val styledAttrs = context.obtainStyledAttributes(it, R.styleable.PasswordEditText, 0, 0)
            val hint = resources.getText(
                styledAttrs.getResourceId(
                    R.styleable.PasswordEditText_PasswordEditTextHint,
                    R.string.setup_enter_password
                )
            )

            setHint(hint as String)

            styledAttrs.recycle()
        }

        binding.passwordEditTextValue.addTextChangedListener {
            val letterSpacing = if (it.isNullOrEmpty()) 0f else 0.4f
            binding.passwordEditTextValue.letterSpacing = letterSpacing
        }
    }

    override fun bind(binding: PasswordEditTextBinding) {
        binding.onShowPasswordClickListener = onShowPasswordClickListener
    }

    private fun setHint(hint: String) {
        binding.passwordEditTextValue.hint = hint
    }

    /**
     * Set the text property of passwordEditTextValue
     */
    fun setTextValue(value: String) {
        binding.passwordEditTextValue.setText(value)
        binding.passwordEditTextValue.setSelection(value.length)
    }

    val getTextValue: String
        get() {
            return binding.passwordEditTextValue.text.toString()
        }

    companion object {
        const val INPUT_TYPE_PASSWORD = 129
        const val INPUT_TYPE_TEXT = 1
    }

    /**
     * Adapters for custom xml fields.
     */
    object BindingAdapters {
        /**
         * Binding Adapter for "textValue".
         *
         * @param passwordEditText The [PasswordEditText] to bind
         * @param value The new value. May come from LiveData
         */
        @BindingAdapter("textValue")
        @JvmStatic
        fun setTextValueAdapter(passwordEditText: PasswordEditText, value: String?) {
            value?.let {
                if (value != passwordEditText.getTextValue) {
                    passwordEditText.setTextValue(value)
                }
            }
        }

        /**
         * "Copied" from Example code.
         * Sets TextWatcher to EditText.
         *
         * @see <a href="https://developer.android.com/reference/android/databinding/InverseBindingMethod">Generated binding classes | Android Developers</a!
         */
        @BindingAdapter(
            value = ["android:afterTextChanged", "android:textAttrChanged"],
            requireAll = false
        )
        @JvmStatic
        fun setTextWatcherAdapter(
            passwordEditText: PasswordEditText,
            test: TextViewBindingAdapter.AfterTextChanged?,
            textAttrChanged: InverseBindingListener?
        ) {
            val newValue = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // No implementation needed
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    // No implementation needed
                }

                @SuppressLint("RestrictedApi")
                override fun afterTextChanged(s: Editable) {
                    test?.let {
                        test.afterTextChanged(s)
                    }

                    textAttrChanged?.let {
                        textAttrChanged.onChange()
                    }
                }
            }
            val oldValue = ListenerUtil.trackListener(
                passwordEditText.binding.passwordEditTextValue,
                newValue,
                R.id.textWatcher
            )
            if (oldValue != null) {
                passwordEditText.binding.passwordEditTextValue.removeTextChangedListener(
                    oldValue
                )
            }
            passwordEditText.binding.passwordEditTextValue.addTextChangedListener(newValue)
        }
    }
}