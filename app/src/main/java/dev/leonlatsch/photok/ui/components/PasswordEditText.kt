package dev.leonlatsch.photok.ui.components

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnClickListener
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.databinding.InverseBindingMethod
import androidx.databinding.InverseBindingMethods
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.PasswordEditTextBinding
import kotlinx.android.synthetic.main.password_edit_text.view.*

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
) : ConstraintLayout(context, attrs, defStyle) {

    private val onShowPasswordClickListener = OnClickListener {
        passwordEditTextValue.inputType = when(passwordEditTextValue.inputType) {
            INPUT_TYPE_PASSWORD -> {
                passwordEditTextIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.eye_closed))
                INPUT_TYPE_TEXT
            }
            INPUT_TYPE_TEXT -> {
                passwordEditTextIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.eye))
                INPUT_TYPE_PASSWORD
            }
            else -> {
                passwordEditTextIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.eye))
                INPUT_TYPE_PASSWORD
            }
        }
    }

    init {
        val binding: PasswordEditTextBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.password_edit_text, this, true)
        binding.onShowPasswordClickListener = onShowPasswordClickListener
        binding.root

        attrs?.let {
            val styledAttrs = context.obtainStyledAttributes(it, R.styleable.PasswordEditText, 0, 0)
            val hint = resources.getText(styledAttrs.getResourceId(R.styleable.PasswordEditText_password_edit_text_hint, 0))

            setHint(hint as String)

            styledAttrs.recycle()
        }

        passwordEditTextValue.addTextChangedListener {
            val letterSpacing = if (it.isNullOrEmpty()) 0f else 0.4f
            passwordEditTextValue.letterSpacing = letterSpacing
        }
    }

    private fun setHint(hint: String) {
        passwordEditTextValue.hint = hint
    }

    fun setTextValue(value: String) {
        passwordEditTextValue.setText(value)
        passwordEditTextValue.setSelection(value.length)
    }

    val getTextValue: String
        get() {
            return passwordEditTextValue.text.toString()
        }

    /**
     * Pass through of addTextChanged to [passwordEditTextValue]
     */
    fun addTextChangedListener(
        beforeTextChanged: (CharSequence?, Int, Int, Int) -> Unit = { _, _, _, _ -> },
        onTextChanged: (CharSequence?, Int, Int, Int) -> Unit = { _, _, _, _ -> },
        afterTextChanged: (Editable?) -> Unit
    ) {
        passwordEditTextValue?.addTextChangedListener(
            beforeTextChanged,
            onTextChanged,
            afterTextChanged
        )
    }

    companion object {
        const val INPUT_TYPE_PASSWORD = 129
        const val INPUT_TYPE_TEXT = 1
    }
}