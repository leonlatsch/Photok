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

package dev.leonlatsch.photok.gallery.ui.navigation

import dev.leonlatsch.photok.main.ui.navigation.AppRoute
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.domain.models.StartPage
import javax.inject.Inject

class GetStartTab @Inject constructor(
    private val config: Config,
) {
    operator fun invoke(): AppRoute = when (StartPage.fromValue(config.galleryStartPage)) {
        StartPage.AllFiles -> AppRoute.Gallery
        StartPage.Albums -> AppRoute.Albums
    }
}
