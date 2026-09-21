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
