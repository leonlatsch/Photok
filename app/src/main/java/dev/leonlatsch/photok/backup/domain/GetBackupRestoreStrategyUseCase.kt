package dev.leonlatsch.photok.backup.domain

import javax.inject.Inject

class GetBackupRestoreStrategyUseCase @Inject constructor(
    private val v1Strategy: RestoreBackupV1,
    private val v2Strategy: RestoreBackupV2,
    private val v3Strategy: RestoreBackupV3,
    private val v4Strategy: RestoreBackupV4,
) {
    operator fun invoke(version: Int): RestoreBackupStrategy? {
        return when (version) {
            1 -> v1Strategy
            2 -> v2Strategy
            3 -> v3Strategy
            4 -> v4Strategy
            else -> null
        }
    }
}