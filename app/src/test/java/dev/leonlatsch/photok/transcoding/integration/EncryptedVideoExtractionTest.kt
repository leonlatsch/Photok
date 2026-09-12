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

package dev.leonlatsch.photok.transcoding.integration

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.DataReader
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.ParsableByteArray
import androidx.media3.datasource.DataSpec
import androidx.media3.extractor.DefaultExtractorInput
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.Extractor
import androidx.media3.extractor.ExtractorOutput
import androidx.media3.extractor.PositionHolder
import androidx.media3.extractor.SeekMap
import androidx.media3.extractor.TrackOutput
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.crypto.CbcCryptoEngine
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.transcoding.data.AesCbcRandomAccessDataSource
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.EOFException
import java.io.File
import java.io.FileOutputStream

private const val FIXTURE_SAMPLE_COUNT = 5
private const val FIXTURE_DURATION_US = 500_000.0
private const val DURATION_TOLERANCE_US = 50_000.0

/**
 * Feeds encrypted videos through [AesCbcRandomAccessDataSource] into Media3's extractors,
 * the same way `ProgressiveMediaSource` does in
 * [dev.leonlatsch.photok.imageviewer.ui.ImageViewerViewModel]: the extractor is picked by
 * sniffing the decrypted stream with [DefaultExtractorsFactory].
 *
 * Every fixture is the same 32x32 half second clip, generated with
 * `ffmpeg -f lavfi -i testsrc=size=32x32:rate=10:duration=0.5 ...` (the per container
 * options are on the individual tests). They live in `app/src/test/resources`.
 *
 * The mdat test covers the Signal-Android regression: an mp4 box with a size field of 0
 * extends to the end of the file, and the extractor can only resolve that through the input
 * length - the value [AesCbcRandomAccessDataSource.open] returns. While it returned
 * `C.LENGTH_UNSET` the mp4 was rejected. A DataSource level test cannot catch this because
 * `C.LENGTH_UNSET` is a legal return value per the DataSource contract.
 */
@RunWith(RobolectricTestRunner::class)
class EncryptedVideoExtractionTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val session = VaultSession(KeyGen().generateVaultMasterKey())
    private val cryptoEngine = CbcCryptoEngine()

    private val sessionRepository = mockk<SessionRepository> {
        every { require() } returns session
    }

    /** `-c:v libx264 -pix_fmt yuv420p -movflags +faststart` */
    @Test
    fun `encrypted mp4 is extracted`() {
        val result = extract(encrypt(readFixture("sample_video.mp4"), "mp4.enc"))

        assertVideoTrack(result, MimeTypes.VIDEO_H264)
        assertEquals(FIXTURE_SAMPLE_COUNT, result.sampleCount)
        assertEquals(FIXTURE_DURATION_US, result.durationUs.toDouble(), DURATION_TOLERANCE_US)
    }

    /** `-c:v libx264 -pix_fmt yuv420p -f mov` */
    @Test
    fun `encrypted mov is extracted`() {
        val result = extract(encrypt(readFixture("sample_video.mov"), "mov.enc"))

        assertVideoTrack(result, MimeTypes.VIDEO_H264)
        assertEquals(FIXTURE_SAMPLE_COUNT, result.sampleCount)
        assertEquals(FIXTURE_DURATION_US, result.durationUs.toDouble(), DURATION_TOLERANCE_US)
    }

    /** `-c:v libx264 -pix_fmt yuv420p -f matroska` */
    @Test
    fun `encrypted mkv is extracted`() {
        val result = extract(encrypt(readFixture("sample_video.mkv"), "mkv.enc"))

        assertVideoTrack(result, MimeTypes.VIDEO_H264)
        assertEquals(FIXTURE_SAMPLE_COUNT, result.sampleCount)
        assertEquals(FIXTURE_DURATION_US, result.durationUs.toDouble(), DURATION_TOLERANCE_US)
    }

    /** `-c:v libvpx-vp9 -pix_fmt yuv420p` */
    @Test
    fun `encrypted webm is extracted`() {
        val result = extract(encrypt(readFixture("sample_video.webm"), "webm.enc"))

        assertVideoTrack(result, MimeTypes.VIDEO_VP9)
        assertEquals(FIXTURE_SAMPLE_COUNT, result.sampleCount)
        assertEquals(FIXTURE_DURATION_US, result.durationUs.toDouble(), DURATION_TOLERANCE_US)
    }

    /**
     * `-c:v mpeg2video -pix_fmt yuv420p -f vob`, an MPEG-2 program stream. `-f mpeg` would
     * write an MPEG-1 system stream, which Media3's PsExtractor does not sniff.
     *
     * A program stream carries no sample table: the reader emits a picture only once the
     * next start code delimits it, so the last frame of the fixture never arrives.
     */
    @Test
    fun `encrypted mpeg is extracted`() {
        val result = extract(encrypt(readFixture("sample_video.mpeg"), "mpeg.enc"))

        assertVideoTrack(result, MimeTypes.VIDEO_MPEG2)
        assertEquals(FIXTURE_SAMPLE_COUNT - 1, result.sampleCount)
    }

    /** Guards that [PhotoType] gained no video type without a fixture to cover it. */
    @Test
    fun `every supported video type has a fixture`() {
        val fixtures = mapOf(
            PhotoType.MP4 to "sample_video.mp4",
            PhotoType.MOV to "sample_video.mov",
            PhotoType.MKV to "sample_video.mkv",
            PhotoType.WEBM to "sample_video.webm",
            PhotoType.MPEG to "sample_video.mpeg",
        )

        assertEquals(PhotoType.entries.filter { it.isVideo }.toSet(), fixtures.keys)
        fixtures.values.forEach { assertTrue("Empty fixture $it", readFixture(it).isNotEmpty()) }
    }

    /** Regression case: Signal on Android writes the mdat box with a size field of 0. */
    @Test
    fun `encrypted mp4 with a zero mdat size is extracted`() {
        val patched = withZeroedMdatSize(readFixture("sample_video.mp4"))
        val result = extract(encrypt(patched, "zero-mdat.enc"))

        assertVideoTrack(result, MimeTypes.VIDEO_H264)
        assertEquals(FIXTURE_SAMPLE_COUNT, result.sampleCount)
        assertEquals(FIXTURE_DURATION_US, result.durationUs.toDouble(), DURATION_TOLERANCE_US)
    }

    private fun assertVideoTrack(result: ExtractionResult, expectedMimeType: String) {
        assertEquals(1, result.trackCount)
        assertNotNull("No video track, formats: ${result.formats}", result.videoFormat)
        assertEquals(expectedMimeType, result.videoFormat?.sampleMimeType)
    }

    private fun extract(file: File): ExtractionResult {
        val dataSource = AesCbcRandomAccessDataSource(sessionRepository)
        val uri = Uri.fromFile(file)
        val output = RecordingExtractorOutput()
        val positionHolder = PositionHolder()

        var input = openInput(dataSource, uri, position = 0)
        val extractor = selectExtractor(input, file.name)

        try {
            extractor.init(output)

            var iterations = 0
            var result = Extractor.RESULT_CONTINUE

            while (result != Extractor.RESULT_END_OF_INPUT) {
                result = extractor.read(input, positionHolder)

                if (result == Extractor.RESULT_SEEK) {
                    dataSource.close()
                    input = openInput(dataSource, uri, positionHolder.position)
                }

                assertTrue("Extractor did not terminate", ++iterations < 10_000)
            }
        } finally {
            extractor.release()
            dataSource.close()
        }

        return ExtractionResult(
            trackCount = output.tracks.size,
            formats = output.tracks.mapNotNull { it.format },
            sampleCount = output.tracks.sumOf { it.sampleCount },
            durationUs = output.seekMap?.durationUs ?: C.TIME_UNSET,
        )
    }

    /** Mirrors how `ProgressiveMediaSource` picks an extractor: sniff until one matches. */
    private fun selectExtractor(input: DefaultExtractorInput, name: String): Extractor {
        for (extractor in DefaultExtractorsFactory().createExtractors()) {
            try {
                if (extractor.sniff(input)) return extractor
            } catch (_: EOFException) {
                // Not this extractor, keep sniffing.
            } finally {
                input.resetPeekPosition()
            }
        }

        error("No Media3 extractor recognized the decrypted stream of $name")
    }

    /** Mirrors how Media3 turns a DataSource into an ExtractorInput. */
    private fun openInput(
        dataSource: AesCbcRandomAccessDataSource,
        uri: Uri,
        position: Long,
    ): DefaultExtractorInput {
        val dataSpec = DataSpec.Builder().setUri(uri).setPosition(position).build()
        val length = dataSource.open(dataSpec)
        val streamLength = if (length == C.LENGTH_UNSET.toLong()) C.LENGTH_UNSET.toLong() else length + position

        return DefaultExtractorInput(dataSource, position, streamLength)
    }

    private fun readFixture(name: String): ByteArray =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(name)) { "Missing test resource $name" }
            .use { it.readBytes() }

    /**
     * Zeroes the 4 byte size field in front of the mdat box type, which is how an mp4 says
     * "this box extends to the end of the file". The mp4 fixture is written with
     * `+faststart`, so mdat is the last box and zeroing its size stays a valid file.
     */
    private fun withZeroedMdatSize(mp4: ByteArray): ByteArray {
        val typeOffset = mp4.indexOfMdatType()
        val sizeOffset = typeOffset - 4
        val declaredSize = ((mp4[sizeOffset].toInt() and 0xFF) shl 24) or
                ((mp4[sizeOffset + 1].toInt() and 0xFF) shl 16) or
                ((mp4[sizeOffset + 2].toInt() and 0xFF) shl 8) or
                (mp4[sizeOffset + 3].toInt() and 0xFF)

        check(sizeOffset + declaredSize == mp4.size) {
            "Fixture mdat must be the last box, declared size $declaredSize at $sizeOffset of ${mp4.size}"
        }

        return mp4.copyOf().also { patched ->
            for (index in sizeOffset until typeOffset) patched[index] = 0
        }
    }

    private fun ByteArray.indexOfMdatType(): Int {
        val type = "mdat".toByteArray(Charsets.US_ASCII)

        outer@ for (start in 4..size - type.size) {
            for (offset in type.indices) {
                if (this[start + offset] != type[offset]) continue@outer
            }
            return start
        }

        error("Fixture contains no mdat box")
    }

    private fun encrypt(plaintext: ByteArray, name: String): File {
        val file = tempFolder.newFile(name)

        FileOutputStream(file).use { fileOutput ->
            cryptoEngine.createEncryptStream(fileOutput, session)!!.use { it.write(plaintext) }
        }

        return file
    }
}

private data class ExtractionResult(
    val trackCount: Int,
    val formats: List<Format>,
    val sampleCount: Int,
    val durationUs: Long,
) {
    val videoFormat: Format?
        get() = formats.firstOrNull { MimeTypes.isVideo(it.sampleMimeType) }
}

private class RecordingExtractorOutput : ExtractorOutput {

    val tracks = mutableListOf<RecordingTrackOutput>()
    var seekMap: SeekMap? = null

    override fun track(id: Int, type: Int): TrackOutput = RecordingTrackOutput().also { tracks += it }

    override fun endTracks() = Unit

    override fun seekMap(seekMap: SeekMap) {
        this.seekMap = seekMap
    }
}

private class RecordingTrackOutput : TrackOutput {

    var format: Format? = null
    var sampleCount = 0
        private set

    override fun format(format: Format) {
        this.format = format
    }

    override fun sampleData(
        input: DataReader,
        length: Int,
        allowEndOfInput: Boolean,
        sampleDataPart: Int,
    ): Int {
        val read = input.read(ByteArray(length), 0, length)

        if (read == C.RESULT_END_OF_INPUT) {
            if (allowEndOfInput) return C.RESULT_END_OF_INPUT
            throw EOFException()
        }

        return read
    }

    override fun sampleData(data: ParsableByteArray, length: Int, sampleDataPart: Int) {
        data.skipBytes(length)
    }

    override fun sampleMetadata(
        timeUs: Long,
        flags: Int,
        size: Int,
        offset: Int,
        cryptoData: TrackOutput.CryptoData?,
    ) {
        sampleCount++
    }
}
