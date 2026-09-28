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
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

/** Matches the duration of the fragment navigation slide animations in `res/anim`. */
const val SlideTransitionDuration = 300

/**
 * Navigating forward: the new content slides in from the right, the old one leaves to the left.
 *
 * Meant for the `transitionSpec` of an `AnimatedContent`, like `res/anim/slide_in_from_right` and
 * `res/anim/slide_out_to_left` are for fragment navigation.
 */
fun slideForward(): ContentTransform =
    slideInHorizontally(tween(SlideTransitionDuration)) { width -> width }
        .togetherWith(
            slideOutHorizontally(tween(SlideTransitionDuration)) { width -> -width }
        )

/**
 * Navigating backward: the new content slides in from the left, the old one leaves to the right.
 *
 * Meant for the `transitionSpec` of an `AnimatedContent`, like `res/anim/slide_in_from_left` and
 * `res/anim/slide_out_to_right` are for fragment navigation.
 */
fun slideBackward(): ContentTransform =
    slideInHorizontally(tween(SlideTransitionDuration)) { width -> -width }
        .togetherWith(
            slideOutHorizontally(tween(SlideTransitionDuration)) { width -> width }
        )
