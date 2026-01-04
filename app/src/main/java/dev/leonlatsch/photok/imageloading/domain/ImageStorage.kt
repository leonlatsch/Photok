package dev.leonlatsch.photok.imageloading.domain

import coil.request.ImageRequest
import java.io.OutputStream

interface ImageStorage {
    suspend fun execAndWrite(
        imageRequest: ImageRequest,
        outputStream: OutputStream?,
    ): Result<Unit>
}