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

package dev.leonlatsch.photok.io

import android.app.Application
import dev.leonlatsch.photok.encryption.domain.crypto.CbcCryptoEngine
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class VaultFileStorageCacheTest {

    private val app: Application = RuntimeEnvironment.getApplication()

    private val storage = VaultFileStorage(
        sessionRepository = mockk { every { get() } returns VaultSession(KeyGen().generateVaultMasterKey()) },
        cryptoEngine = CbcCryptoEngine(),
        app = app,
    )

    @After
    fun tearDown() {
        File(app.cacheDir, "dir").deleteRecursively()
    }

    @Test
    fun `cache files are encrypted and read back`() {
        val content = "content".toByteArray()

        storage.openEncryptedCacheOutput("dir/a")!!.use { it.write(content) }

        assertFalse(File(app.cacheDir, "dir/a").readBytes().contentEquals(content))
        assertArrayEquals(content, storage.openEncryptedCacheInput("dir/a")!!.use { it.readBytes() })
    }

    @Test
    fun `cache files are not stored in the vault files`() {
        storage.openEncryptedCacheOutput("dir/a")!!.use { it.write(byteArrayOf(1)) }

        assertTrue(storage.cacheFileExists("dir/a"))
        assertFalse(app.getFileStreamPath("a").exists())
    }

    @Test
    fun `renamed cache file replaces the temporary one`() {
        storage.openEncryptedCacheOutput("dir/a.tmp")!!.use { it.write(byteArrayOf(1)) }

        assertTrue(storage.renameCacheFile("dir/a.tmp", "dir/a"))

        assertEquals(listOf("a"), storage.listCacheFiles("dir").map { it.name })
    }

    @Test
    fun `deleting a cache dir deletes everything in it`() {
        storage.openEncryptedCacheOutput("dir/sub/a")!!.use { it.write(byteArrayOf(1)) }

        assertTrue(storage.deleteCacheDir("dir"))

        assertFalse(storage.cacheFileExists("dir/sub/a"))
        assertEquals(emptyList<File>(), storage.listCacheFiles("dir"))
    }
}
