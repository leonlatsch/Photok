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