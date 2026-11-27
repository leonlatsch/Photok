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

package dev.leonlatsch.photok.ui.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

const val FadeTransitionDuration = 300

/** The new content fades in over the old one, which stays in place underneath. */
fun fadeInOver(): ContentTransform =
    fadeIn(tween(FadeTransitionDuration))
        .togetherWith(fadeOut(tween(FadeTransitionDuration), targetAlpha = 1f))

/** The old content fades out and reveals the new one, which is already in place underneath. */
fun fadeOutReveal(): ContentTransform =
    EnterTransition.None
        .togetherWith(fadeOut(tween(FadeTransitionDuration)))
