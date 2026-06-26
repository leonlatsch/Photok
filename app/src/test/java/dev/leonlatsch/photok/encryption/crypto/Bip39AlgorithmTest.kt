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

package dev.leonlatsch.photok.encryption.crypto

import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.crypto.entropyToWords
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class Bip39AlgorithmTest {

    private val wordlist: List<String> by lazy {
        File("src/main/res/raw/bip39_english.txt")
            .readLines()
            .filter { it.isNotBlank() }
    }

    /**
     * Official BIP-39 test vector (all-zeros 128-bit entropy).
     * All-zeros entropy produces 11 "abandon" words + the checksum word "about".
     * A regression here means the key derivation path is broken for anyone recovering their vault.
     */
    @Test
    fun `all-zeros 16-byte entropy matches BIP-39 test vector`() {
        val entropy = ByteArray(16) { 0 }
        val words = entropyToWords(entropy, wordlist)

        assertEquals(12, words.size)
        assertEquals(List(11) { "abandon" } + listOf("about"), words)
    }

    /**
     * Same test vector for 256-bit entropy: 23 "abandon" words + checksum word "art".
     */
    @Test
    fun `all-zeros 32-byte entropy matches BIP-39 test vector`() {
        val entropy = ByteArray(32) { 0 }
        val words = entropyToWords(entropy, wordlist)

        assertEquals(24, words.size)
        assertEquals(List(23) { "abandon" } + listOf("art"), words)
    }

    @Test
    fun `16-byte entropy produces 12 words`() {
        val words = entropyToWords(ByteArray(Bip39WordCount.Twelve.entropyBytes) { it.toByte() }, wordlist)
        assertEquals(Bip39WordCount.Twelve.words, words.size)
    }

    @Test
    fun `32-byte entropy produces 24 words`() {
        val words = entropyToWords(ByteArray(Bip39WordCount.TwentyFour.entropyBytes) { it.toByte() }, wordlist)
        assertEquals(Bip39WordCount.TwentyFour.words, words.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid entropy size throws`() {
        entropyToWords(ByteArray(15), wordlist)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `wrong wordlist size throws`() {
        entropyToWords(ByteArray(16), List(100) { "word" })
    }

    @Test
    fun `different entropy inputs produce different word sequences`() {
        val entropy1 = ByteArray(16) { it.toByte() }
        val entropy2 = ByteArray(16) { (it + 1).toByte() }

        val words1 = entropyToWords(entropy1, wordlist)
        val words2 = entropyToWords(entropy2, wordlist)

        assertNotEquals(words1, words2)
    }

    @Test
    fun `all output words are present in the BIP-39 wordlist`() {
        val entropy = ByteArray(16) { (it * 7).toByte() }
        val words = entropyToWords(entropy, wordlist)

        words.forEach { word ->
            assertFalse("Word '$word' not found in wordlist", !wordlist.contains(word))
        }
    }
}
