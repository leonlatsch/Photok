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

package dev.leonlatsch.photok.backup.domain

sealed interface RestoreDuplicates {
    data class Skip(val uuids: Set<String>) : RestoreDuplicates
    data class ImportAgain(val newUuids: Map<String, String>) : RestoreDuplicates
}

fun RestoreDuplicates.isSkipped(uuid: String): Boolean = when (this) {
    is RestoreDuplicates.Skip -> uuid in uuids
    is RestoreDuplicates.ImportAgain -> false
}

fun RestoreDuplicates.vaultUuid(uuid: String): String = when (this) {
    is RestoreDuplicates.Skip -> uuid
    is RestoreDuplicates.ImportAgain -> newUuids[uuid] ?: uuid
}
