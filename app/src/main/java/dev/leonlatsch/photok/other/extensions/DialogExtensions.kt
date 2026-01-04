package dev.leonlatsch.photok.other.extensions

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

/**
 * Show a DialogFragment with its simple class name as tag.
 */
fun DialogFragment.show(fragmentManager: FragmentManager) {
    this.show(fragmentManager, this::class.simpleName)
}