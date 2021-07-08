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

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dev.leonlatsch.photok.BaseApplication
import kotlin.reflect.KClass

/**
 * Require the parent activity as a specific type to avoid casting.
 *
 * @see Fragment.requireActivity
 */
@Suppress("UNCHECKED_CAST")
fun <T : AppCompatActivity> Fragment.requireActivityAs(clazz: KClass<T>): T {
    val activity = requireActivity()
    return try {
        activity as T
    } catch (e: ClassCastException) {
        throw IllegalArgumentException("$activity is not of type ${clazz.simpleName}")
    }
}

/**
 * Extension for starting an activity for result and disable lock timer in [BaseApplication].
 */
fun Fragment.startActivityForResultAndIgnoreTimer(intent: Intent, reqCode: Int) {
    startActivityForResult(intent, reqCode)
    requireActivity().getBaseApplication().ignoreNextTimeout()
}