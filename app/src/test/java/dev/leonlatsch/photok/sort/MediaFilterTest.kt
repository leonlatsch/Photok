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

package dev.leonlatsch.photok.sort

import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.sort.domain.MediaFilter
import dev.leonlatsch.photok.sort.domain.filterByMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class MediaFilterTest {

    private val allTypes = PhotoType.entries.toList()

    @Test
    fun `all keeps every type and returns the same list`() {
        val result = allTypes.filterByMediaType(MediaFilter.All) { it }

        assertSame(allTypes, result)
    }

    @Test
    fun `photos keeps only non video types`() {
        val result = allTypes.filterByMediaType(MediaFilter.Photos) { it }

        assertEquals(allTypes.filterNot { it.isVideo }, result)
    }

    @Test
    fun `videos keeps only video types`() {
        val result = allTypes.filterByMediaType(MediaFilter.Videos) { it }

        assertEquals(
            listOf(PhotoType.MP4, PhotoType.MPEG, PhotoType.WEBM, PhotoType.MOV, PhotoType.MKV),
            result,
        )
    }

    @Test
    fun `filtering keeps the original order`() {
        val input = listOf(PhotoType.MKV, PhotoType.PNG, PhotoType.MP4, PhotoType.HEIC, PhotoType.GIF)

        assertEquals(
            listOf(PhotoType.PNG, PhotoType.HEIC, PhotoType.GIF),
            input.filterByMediaType(MediaFilter.Photos) { it },
        )
        assertEquals(
            listOf(PhotoType.MKV, PhotoType.MP4),
            input.filterByMediaType(MediaFilter.Videos) { it },
        )
    }
}
