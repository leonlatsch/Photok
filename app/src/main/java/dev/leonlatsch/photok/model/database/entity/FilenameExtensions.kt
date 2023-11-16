/*
 *   Copyright 2020-2023 Leon Latsch
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

package dev.leonlatsch.photok.model.database.entity

/**
 * Get FileName for internal files and backup files.
 * Sample: 923ae2b7-f056-453d-a3dc-264a08e58a07.photok
 */
fun internalFileName(uuid: String) = "${uuid}.photok"

/**
 * Get FileName for internal thumbnails.
 * Sample: 923ae2b7-f056-453d-a3dc-264a08e58a07.photok.tn
 */
fun internalThumbnailFileName(uuid: String) = "${uuid}.photok.tn"

/**
 * Get FileName for video previews.
 * Sample: 923ae2b7-f056-453d-a3dc-264a08e58a07.photok.vp
 */
fun internalVideoPreviewFileName(uuid: String) = "${uuid}.photok.vp"