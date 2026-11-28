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

package dev.leonlatsch.photok.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints

/** Measures a sideways photo against the transposed viewport before rotating it into place. */
internal fun Modifier.rotatePhoto(rotationDegrees: Int): Modifier {
    if (rotationDegrees != 90 && rotationDegrees != 270) return rotate(rotationDegrees.toFloat())

    return layout { measurable, constraints ->
        val photo = measurable.measure(
            Constraints(
                minWidth = constraints.minHeight,
                maxWidth = constraints.maxHeight,
                minHeight = constraints.minWidth,
                maxHeight = constraints.maxWidth,
            )
        )
        layout(photo.height, photo.width) {
            photo.placeWithLayer(0, 0) {
                rotationZ = rotationDegrees.toFloat()
                translationX = (photo.height - photo.width) / 2f
                translationY = -translationX
            }
        }
    }
}
