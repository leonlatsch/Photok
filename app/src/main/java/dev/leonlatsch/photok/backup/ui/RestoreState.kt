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

package dev.leonlatsch.photok.backup.ui

/**
 * Enum for state of [RestoreBackupDialogFragment]
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
enum class RestoreState {
    INITIALIZE,
    FILE_VALID,
    FILE_INVALID,
    RESTORING,
    FINISHED,
    FINISHED_WITH_ERRORS,
}