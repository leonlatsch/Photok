package dev.leonlatsch.photok.backup.domain

import dev.leonlatsch.photok.backup.data.PhotoBackup
import dev.leonlatsch.photok.model.database.entity.PhotoType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val PHOTO_SIZE = 20_000_000L
private const val CHUNK = 8192L

/** ~100 MB/s, so one chunk takes 0.08 ms. Counted in micros to stay on whole numbers. */
private const val MICROS_PER_CHUNK = 80L

/** One photo of [PHOTO_SIZE] takes about this long to copy at the speed above. */
private const val MILLIS_PER_PHOTO = 195L

class RestoreProgressTrackerTest {

    private var micros = 0L

    private fun photos(count: Int, size: Long = PHOTO_SIZE) = (1..count).map {
        PhotoBackup(
            fileName = "photo_$it.jpg",
            importedAt = 0,
            lastModified = null,
            type = PhotoType.JPEG,
            size = size,
            uuid = "uuid-$it",
        )
    }

    private fun tracker(photos: List<PhotoBackup>) =
        RestoreProgressTracker(photos) { micros / 1000 }

    private fun RestoreProgressTracker.copyFile(photo: PhotoBackup, bytes: Long = PHOTO_SIZE) {
        startFile(photo)

        var copied = 0L
        while (copied < bytes) {
            copied += CHUNK
            micros += MICROS_PER_CHUNK
            advance(CHUNK)
        }
    }

    @Test
    fun `reports no estimate in the first moments of the restore`() {
        val photos = photos(count = 10)
        val tracker = tracker(photos)

        tracker.startFile(photos.first())
        micros += MICROS_PER_CHUNK
        val progress = tracker.snapshot()

        assertEquals(0, progress.filesDone)
        assertNull(progress.millisRemaining)
    }

    @Test
    fun `estimates while the first file is still being copied`() {
        // A backup whose first file holds most of the bytes, the case that used to stay
        // on "Estimating…" until that file was done
        val photos = photos(count = 1, size = 9 * PHOTO_SIZE) + photos(count = 9)
        val tracker = tracker(photos)

        // Half of the big file, a quarter of the backup
        tracker.copyFile(photos.first(), bytes = 9 * PHOTO_SIZE / 2)
        val progress = tracker.snapshot()

        assertEquals(0, progress.filesDone)

        // A quarter took ~880 ms, the remaining three quarters need ~2640 ms
        val remaining = requireNotNull(progress.millisRemaining)
        assertEquals(3 * 880L, remaining, 200.0)
    }

    @Test
    fun `estimates the remaining time from the declared sizes`() {
        val photos = photos(count = 10)
        val tracker = tracker(photos)

        repeat(5) { tracker.copyFile(photos[it]) }
        tracker.copyFile(photos[5])
        val progress = tracker.finishFile()

        assertEquals(1, progress.filesDone)
        assertEquals(PHOTO_SIZE, progress.bytesDone)
        assertEquals(10 * PHOTO_SIZE, progress.bytesTotal)

        // 6 files worth of time elapsed for 1 file of progress -> 9 files still to go
        val remaining = requireNotNull(progress.millisRemaining)
        assertEquals(9 * 6 * MILLIS_PER_PHOTO, remaining, 300.0)
    }

    @Test
    fun `falls back to the file count when the declared sizes are zero`() {
        val photos = photos(count = 10, size = 0L)
        val tracker = tracker(photos)

        repeat(4) {
            tracker.copyFile(photos[it], bytes = PHOTO_SIZE)
            tracker.finishFile()
        }

        val progress = tracker.snapshot()

        assertEquals(4, progress.filesDone)
        assertEquals(0L, progress.bytesTotal)

        // 4 files took ~780 ms, the remaining 6 should be estimated at ~1170 ms
        val remaining = requireNotNull(progress.millisRemaining)
        assertEquals(6 * MILLIS_PER_PHOTO, remaining, 100.0)
    }

    @Test
    fun `measures the speed on the bytes actually copied`() {
        val photos = photos(count = 10, size = 0L)
        val tracker = tracker(photos)

        tracker.copyFile(photos.first(), bytes = PHOTO_SIZE)
        val progress = tracker.finishFile()

        // 100 MB/s, allow for the chunk rounding
        assertEquals(100_000_000.0, progress.bytesPerSecond.toDouble(), 8_000_000.0)
    }

    @Test
    fun `reports the file currently being restored`() {
        val photos = photos(count = 10)
        val tracker = tracker(photos)

        assertNull(tracker.snapshot().currentFile)

        tracker.copyFile(photos[0])
        tracker.finishFile()
        tracker.copyFile(photos[1])

        assertEquals(
            RestoreProgress.CurrentFile(index = 2, fileName = "photo_2.jpg"),
            tracker.snapshot().currentFile,
        )

        // The finish emit still names the file, so short files show up in the log
        assertEquals(
            RestoreProgress.CurrentFile(index = 2, fileName = "photo_2.jpg"),
            tracker.finishFile().currentFile,
        )
    }

    private fun assertEquals(expected: Long, actual: Long, delta: Double) =
        assertEquals(expected.toDouble(), actual.toDouble(), delta)
}
