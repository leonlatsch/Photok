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

package dev.leonlatsch.photok.settings.domain

import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.pro.paniclock.PanicLockMotion
import dev.leonlatsch.photok.pro.passwordattempts.PasswordAttemptsLimit
import dev.leonlatsch.photok.settings.data.Config

object ProPreferences {

    val PanicLock = Preference.Enum(
        key = Config.SECURITY_PANIC_LOCK,
        icon = R.drawable.ic_panic_lock,
        title = R.string.settings_pro_panic_lock_title,
        explanation = R.string.settings_pro_panic_lock_summary,
        default = PanicLockMotion.None,
        possibleValues = PanicLockMotion.entries,
        proFeature = true,
    )

    val PasswordAttempts = Preference.Enum(
        key = Config.SECURITY_MAX_PASSWORD_ATTEMPTS,
        icon = R.drawable.ic_key,
        title = R.string.settings_pro_password_attempts_title,
        explanation = R.string.settings_pro_password_attempts_summary,
        default = PasswordAttemptsLimit.Unlimited,
        possibleValues = PasswordAttemptsLimit.entries,
        proFeature = true,
    )
}
