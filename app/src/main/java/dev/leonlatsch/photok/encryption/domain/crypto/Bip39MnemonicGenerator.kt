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

package dev.leonlatsch.photok.encryption.domain.crypto

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.R
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates BIP-39 mnemonic recovery phrases.
 *
 * Algorithm (per BIP-39 spec):
 * 1. Generate N bytes of secure random entropy.
 * 2. Compute SHA-256 of the entropy; take the first (entropyBits / 32) bits as checksum.
 * 3. Concatenate entropy bits + checksum bits → total bits divisible by 11.
 * 4. Split into groups of 11 bits; map each group to a word in the 2048-word list.
 */
@Singleton
class Bip39MnemonicGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val wordlist: List<String> by lazy {
        context.resources.openRawResource(R.raw.bip39_english)
            .bufferedReader()
            .readLines()
            .filter { it.isNotBlank() }
    }

    /**
     * Generates a BIP-39 mnemonic phrase.
     *
     * @param wordCount The desired phrase length (12 or 24 words).
     * @return An ordered list of mnemonic words.
     */
    fun generate(wordCount: Bip39WordCount = Bip39WordCount.Twelve): List<String> {
        val entropy = ByteArray(wordCount.entropyBytes).also { SecureRandom().nextBytes(it) }
        return entropyToMnemonic(entropy)
    }

    /**
     * Converts raw entropy bytes to a BIP-39 mnemonic word list.
     * Useful for deterministic tests with a known entropy value.
     */
    fun entropyToMnemonic(entropy: ByteArray): List<String> =
        entropyToWords(entropy, wordlist)
}

/**
 * Pure BIP-39 algorithm: maps entropy bytes to mnemonic words from the given list.
 * Internal so tests in the same package can call it directly without Android context.
 */
internal fun entropyToWords(entropy: ByteArray, wordlist: List<String>): List<String> {
    require(entropy.size == 16 || entropy.size == 32) {
        "Entropy must be 16 bytes (12 words) or 32 bytes (24 words)"
    }
    require(wordlist.size == 2048) { "Wordlist must contain exactly 2048 words" }

    val checksumBits = entropy.size / 4 // entropyBits / 32
    val checksum = MessageDigest.getInstance("SHA-256").digest(entropy)

    // Build a bit string: all entropy bits + first `checksumBits` bits of SHA-256
    val totalBits = entropy.size * 8 + checksumBits
    val bits = BooleanArray(totalBits)

    for (i in entropy.indices) {
        for (bit in 0 until 8) {
            bits[i * 8 + bit] = (entropy[i].toInt() shr (7 - bit) and 1) == 1
        }
    }
    for (bit in 0 until checksumBits) {
        bits[entropy.size * 8 + bit] = (checksum[0].toInt() shr (7 - bit) and 1) == 1
    }

    // Each word encodes 11 bits → totalBits / 11 words
    return List(totalBits / 11) { wordIndex ->
        val value = (0 until 11).fold(0) { acc, bit ->
            (acc shl 1) or (if (bits[wordIndex * 11 + bit]) 1 else 0)
        }
        wordlist[value]
    }
}
