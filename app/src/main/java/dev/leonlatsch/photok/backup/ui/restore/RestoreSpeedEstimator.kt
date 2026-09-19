package dev.leonlatsch.photok.backup.ui.restore

/**
 * Estimates the current restore speed over a sliding window. A cumulative average
 * (total bytes / total time) reacts far too slowly to be useful for a time remaining.
 */
class RestoreSpeedEstimator(private val windowMillis: Long = 5_000) {

    private data class Sample(val at: Long, val bytes: Long)

    private val samples = ArrayDeque<Sample>()

    fun bytesPerSecond(bytesDone: Long, now: Long = System.currentTimeMillis()): Long {
        samples.addLast(Sample(at = now, bytes = bytesDone))

        while (samples.size > 1 && now - samples.first().at > windowMillis) {
            samples.removeFirst()
        }

        val oldest = samples.first()
        val elapsed = now - oldest.at
        if (elapsed <= 0) return 0

        return (bytesDone - oldest.bytes) * 1000 / elapsed
    }
}
