/*
 *   Copyright 2020–2026 Leon Latsch
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

package dev.leonlatsch.photok.forwarddialer

import android.content.Intent
import dev.leonlatsch.photok.main.ui.MainActivity
import javax.inject.Inject

class ForwardDialerNavigator @Inject constructor() {

    fun navigate(navigationEvent: NavigationEvent, activity: ForwardDialerActivity) {
        when (navigationEvent) {
            NavigationEvent.ForwardToDialer -> navigateForwardToDialer(activity)
            NavigationEvent.ForwardToApp -> navigateToApp(activity)
        }
    }

    private fun navigateToApp(activity: ForwardDialerActivity) {
        val launchIntent = Intent(activity, MainActivity::class.java)
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(launchIntent)
    }

    private fun navigateForwardToDialer(activity: ForwardDialerActivity) = runCatching {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity.apply {
            startActivity(dialIntent)
            finishAndRemoveTask()
        }
    }

    sealed class NavigationEvent {
        object ForwardToApp : NavigationEvent()
        object ForwardToDialer : NavigationEvent()
    }
}