/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.sort.domain

enum class SortConfig(
    val fields: List<Sort.Field>,
    val default: Sort,
) {
    Gallery(
        fields = listOf(Sort.Field.ImportDate, Sort.Field.FileName, Sort.Field.Size),
        default = Sort(field = Sort.Field.ImportDate, Sort.Order.Desc),
    ),
    Album(
        fields = listOf(Sort.Field.LinkedAt, Sort.Field.FileName, Sort.Field.Size),
        default = Sort(field = Sort.Field.LinkedAt, Sort.Order.Desc),
    ),
}