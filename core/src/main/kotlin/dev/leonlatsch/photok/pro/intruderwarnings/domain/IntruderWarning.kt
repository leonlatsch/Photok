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

package dev.leonlatsch.photok.pro.intruderwarnings.domain

import java.util.Date
import java.util.UUID

data class IntruderWarning(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Date = Date(),
    val type: IntruderWarningType,
)

enum class IntruderWarningType(val value: String) {
    Password("password"),
    Biometrics("biometrics");

    companion object {
        fun fromValue(value: String): IntruderWarningType {
            return when (value) {
                Password.value -> Password
                Biometrics.value -> Biometrics
                else -> Password
            }
        }
    }
}
