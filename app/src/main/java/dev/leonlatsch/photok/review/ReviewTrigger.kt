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

import android.content.Context
import dev.leonlatsch.photok.BuildConfig
import kotlin.time.Duration.Companion.days

enum class ReviewTrigger {
    Import {
        override fun meetsRequirements(context: Context): Boolean {
            if (BuildConfig.DEBUG) return true

            val installTime = runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
            }.getOrDefault(Long.MAX_VALUE)

            return System.currentTimeMillis() - installTime >= 3.days.inWholeMilliseconds
        }
    },
    BackupRestored {
        override fun meetsRequirements(context: Context) = true
    },
    RecoveryPhraseUsed {
        override fun meetsRequirements(context: Context) = true
    };

    abstract fun meetsRequirements(context: Context): Boolean
}
