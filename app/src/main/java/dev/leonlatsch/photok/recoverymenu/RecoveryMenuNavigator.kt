/*
 *   Copyright 2020-2022 Leon Latsch
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

package dev.leonlatsch.photok.recoverymenu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import dev.leonlatsch.photok.main.ui.MainActivity
import javax.inject.Inject

class RecoveryMenuNavigator @Inject constructor() {

    fun navigate(navigationEvent: NavigationEvent, activity: AppCompatActivity) {
        when (navigationEvent) {
            NavigationEvent.OpenPhotok -> navigateOpenPhotok(activity)
            NavigationEvent.AfterResetHideApp -> navigateAfterResetHideApp(activity)
        }
    }

    private fun navigateAfterResetHideApp(activity: AppCompatActivity) {
        activity.finish()
    }

    private fun navigateOpenPhotok(activity: AppCompatActivity) {
        val intent = Intent(activity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activity.startActivity(intent)
        activity.finish()
    }

    sealed class NavigationEvent {
        object OpenPhotok : NavigationEvent()
        object AfterResetHideApp : NavigationEvent()
    }
}