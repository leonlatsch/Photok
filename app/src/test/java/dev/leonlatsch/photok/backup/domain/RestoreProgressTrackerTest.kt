package dev.leonlatsch.photok.backup.domain

import dev.leonlatsch.photok.backup.data.PhotoBackup
import dev.leonlatsch.photok.model.database.entity.PhotoType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val PHOTO_SIZE = 2_000_000L
private const val CHUNK = 8192L

/** 100 MB/s, so one chunk takes 0.08 ms. Counted in micros to stay on whole numbers. */
private const val MICROS_PER_CHUNK = 80L

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
    fun `reports no estimate before the first file finished`() {
        val photos = photos(count = 10)
        val tracker = tracker(photos)

        tracker.startFile(photos.first())
        micros += MICROS_PER_CHUNK
        val progress = tracker.snapshot()

        assertEquals(0, progress.filesDone)
        assertNull(progress.millisRemaining)
    }

    @Test
    fun `estimates the remaining time from the declared sizes`() {
        val photos = photos(count = 10)
        val tracker = tracker(photos)

        // 10 files at 100 MB/s -> 200 ms total, 20 ms per file
        repeat(5) { tracker.copyFile(photos[it]) }
        tracker.copyFile(photos[5])
        val progress = tracker.finishFile()

        assertEquals(1, progress.filesDone)
        assertEquals(PHOTO_SIZE, progress.bytesDone)
        assertEquals(10 * PHOTO_SIZE, progress.bytesTotal)

        // 6 files worth of time elapsed for 1 file of progress -> 9 files still to go
        val remaining = requireNotNull(progress.millisRemaining)
        assertEquals(9 * 120L, remaining, 50.0)
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

        // 4 files took 80 ms, the remaining 6 should be estimated at ~120 ms
        val remaining = requireNotNull(progress.millisRemaining)
        assertEquals(120L, remaining, 50.0)
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
