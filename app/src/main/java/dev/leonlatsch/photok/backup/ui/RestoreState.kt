package dev.leonlatsch.photok.backup.ui

/**
 * Enum for state of [RestoreBackupDialogFragment]
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
enum class RestoreState {
    INITIALIZE,
    FILE_VALID,
    FILE_INVALID,
    RESTORING,
    FINISHED,
    FINISHED_WITH_ERRORS,
}