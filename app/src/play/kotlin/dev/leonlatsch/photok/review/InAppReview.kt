/*
 *   Copyright 2020-2026 Leon Latsch
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

package dev.leonlatsch.photok.review

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.settings.data.Config
import javax.inject.Inject

class InAppReviewImpl @Inject constructor(
    private val config: Config,
    @ApplicationContext private val context: Context,
) : InAppReview {

    override suspend fun requestInAppReview(activity: Activity) {
        if (config.inAppReviewRequested) return
        if (!isInstallOldEnough()) return

        config.inAppReviewRequested = true

        val manager = if (BuildConfig.DEBUG) {
            FakeReviewManager(activity)
        } else {
            ReviewManagerFactory.create(activity)
        }
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                manager.launchReviewFlow(activity, task.result)
            }
        }
    }

    private fun isInstallOldEnough(): Boolean {
        if (BuildConfig.DEBUG) return true
        val installTime = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        }.getOrDefault(Long.MAX_VALUE)
        return System.currentTimeMillis() - installTime >= SEVEN_DAYS_MS
    }

    companion object {
        private const val SEVEN_DAYS_MS = 7L * 24 * 60 * 60 * 1000
    }
}
