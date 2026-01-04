package dev.leonlatsch.photok.backup.data

import com.google.gson.Gson
import java.util.zip.ZipInputStream
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ReadBackupMetadataUseCase @Inject constructor(
    private val gson: Gson
) {
    suspend operator fun invoke(zipInputStream: ZipInputStream): BackupMetaData =
        suspendCoroutine { continuation ->
            val bytes = zipInputStream.readBytes()
            val string = String(bytes)

            val metaData = gson.fromJson(string, BackupMetaData::class.java)
            metaData ?: error("Error reading meta json from $zipInputStream")

            continuation.resume(metaData)
        }
}