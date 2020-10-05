/*
 *   Copyright 2020 Leon Latsch
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

package dev.leonlatsch.photok.other

/**
 * Holds the keys and the defaults for the apps config.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
object Config {

    /**
     * Determines if the app has started before.
     */
    const val SYSTEM_FIRST_START = "system^firstStart"
    const val SYSTEM_FIRST_START_DEFAULT  = true

    /**
     * Determines if the full screen photo view, should hide the system ui at start.
     */
    const val GALLERY_AUTO_FULLSCREEN = "gallery^fullscreen.auto"
    const val GALLERY_AUTO_FULLSCREEN_DEFAULT = true

    /**
     * Determines the thumbnail size.
     */
    const val GALLERY_ADVANCED_THUMBNAIL_SIZE = "gallery^advanced.thumbnailSize"
    const val GALLERY_ADVANCED_THUMBNAIL_SIZE_DEFAULT = 128

    /**
     * Determines the gallery columns.
     */
    const val GALLERY_ADVANCED_GALLERY_COLUMNS = "gallery^advanced.galleryColumns"
    const val GALLERY_ADVANCED_GALLERY_COLUMNS_DEFAULT = 4

    /**
     * Determines if screenshots should be allowed.
     */
    const val SECURITY_ALLOW_SCREENSHOTS = "security^allowScreenshots"
    const val SECURITY_ALLOW_SCREENSHOTS_DEFAULT = false
}