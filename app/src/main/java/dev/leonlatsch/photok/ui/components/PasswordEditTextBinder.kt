package dev.leonlatsch.photok.ui.components

import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.databinding.adapters.ListenerUtil
import androidx.databinding.adapters.TextViewBindingAdapter
import dev.leonlatsch.photok.R
import kotlinx.android.synthetic.main.password_edit_text.view.*

object PasswordEditTextBinder {

    @JvmStatic
    @BindingAdapter("textValue")
    fun setText(passwordEditText: PasswordEditText, value: String?) {
        value?.let {
            passwordEditText.setTextValue(it)
        }
    }

    /**
     * "Copied" from Example code.
     * Sets TextWatcher to EditText.
     *
     * @see <a href="https://developer.android.com/reference/android/databinding/InverseBindingMethod">Generated binding classes | Android Developers</a!
     */
    @JvmStatic
    @BindingAdapter(
        value = ["android:afterTextChanged", "android:textAttrChanged"],
        requireAll = false
    )
    fun setTextWatcher(
        passwordEditText: PasswordEditText,
        test: TextViewBindingAdapter.AfterTextChanged?,
        textAttrChanged: InverseBindingListener?
    ) {
        val newValue = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
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
            passwordEditText.passwordEditTextValue,
            newValue,
            R.id.textWatcher
        )
        if (oldValue != null) {
            passwordEditText.passwordEditTextValue.removeTextChangedListener(oldValue)
        }
        passwordEditText.passwordEditTextValue.addTextChangedListener(newValue)
    }

}