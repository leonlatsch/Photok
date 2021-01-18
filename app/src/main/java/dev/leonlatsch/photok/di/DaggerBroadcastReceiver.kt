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

package dev.leonlatsch.photok.di

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Workaround class for injecting into broadcast receiver.
 * This should be remove, once hilt fixes this.
 * Ensures super call to [onReceive].
 *
 * More information: https://github.com/google/dagger/issues/1918
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
abstract class DaggerBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {}
}