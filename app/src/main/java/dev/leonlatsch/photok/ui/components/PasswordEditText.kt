package dev.leonlatsch.photok.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import dev.leonlatsch.photok.R
import kotlinx.android.synthetic.main.password_edit_text.view.*

class PasswordEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    init {
        LayoutInflater.from(context).inflate(R.layout.password_edit_text, this, true)

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
}