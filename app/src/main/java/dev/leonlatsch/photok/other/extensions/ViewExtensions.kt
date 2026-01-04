package dev.leonlatsch.photok.other.extensions

import android.view.View

/**
 * Sets the visibility to [View.VISIBLE]
 */
fun View.show() {
    this.visibility = View.VISIBLE
}

/**
 * Sets the visibility to [View.GONE]
 */
fun View.hide() {
    this.visibility = View.GONE
}

/**
 * Sets the visibility to [View.INVISIBLE]
 */
fun View.vanish() {
    this.visibility = View.INVISIBLE
}