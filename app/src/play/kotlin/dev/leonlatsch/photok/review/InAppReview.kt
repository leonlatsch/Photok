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
import android.widget.Toast
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.pro.SampleProFeature
import dev.leonlatsch.photok.settings.data.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class InAppReviewImpl @Inject constructor(
    private val config: Config,
    @ApplicationContext private val context: Context,
    private val appScope: CoroutineScope,
    private val sampleProFeature: SampleProFeature,
) : InAppReview {

    override fun requestInAppReview(activity: Activity, trigger: ReviewTrigger) {
        sampleProFeature.show()
        if (config.inAppReviewRequested) return
        if (!trigger.meetsRequirements(context)) return

        appScope.launch {
            delay(3.seconds)

            withContext(Dispatchers.Main) {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(activity, "DEBUG Review Request", Toast.LENGTH_LONG).show()
                    config.inAppReviewRequested = true
                    return@withContext
                }

                val manager = ReviewManagerFactory.create(activity)
                val request = manager.requestReviewFlow()

                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        config.inAppReviewRequested = true
                        manager.launchReviewFlow(activity, task.result)
                    }
                }
            }
        }
    }

}
