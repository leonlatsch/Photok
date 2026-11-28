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

package dev.leonlatsch.photok.model.database

import androidx.room.Room
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PhotoRotationTest {

    private val database = Room.inMemoryDatabaseBuilder(
        RuntimeEnvironment.getApplication(),
        PhotokDatabase::class.java,
    ).build()

    @After
    fun closeDatabase() = database.close()

    @Test
    fun `clockwise taps accumulate on the selected photo and wrap after four turns`() = runTest {
        val photos = database.getPhotoDao()
        val first = Photo("first.jpg", 1L, PhotoType.JPEG, lastModified = null)
        val second = Photo("second.jpg", 2L, PhotoType.JPEG, lastModified = null)
        photos.insertAll(listOf(first, second))

        for (expected in listOf(90, 180, 270, 0)) {
            photos.rotateClockwise(first.uuid)
            assertEquals(first.copy(rotationDegrees = expected), photos.get(first.uuid))
        }

        coroutineScope {
            repeat(5) { launch { photos.rotateClockwise(first.uuid) } }
        }
        assertEquals(first.copy(rotationDegrees = 90), photos.get(first.uuid))
        assertEquals(second, photos.get(second.uuid))
    }
}
