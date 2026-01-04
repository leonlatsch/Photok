


package dev.leonlatsch.photok.imageloading.data

import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import dev.leonlatsch.photok.imageloading.domain.ImageStorage
import dev.leonlatsch.photok.other.extensions.writeTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ImageStorageImpl @Inject constructor(
    private val imageLoader: ImageLoader,
) : ImageStorage {

    /**
     * Executes an [imageRequest] and writes its result to the desired [outputStream].
     */
    override suspend fun execAndWrite(
        imageRequest: ImageRequest,
        outputStream: OutputStream?,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        outputStream ?: return@withContext Result.failure(Exception("stream is null"))

        when (val imageResult = imageLoader.execute(imageRequest)) {
            is SuccessResult -> suspendCoroutine { continuation ->
                try {
                    outputStream.use { out ->
                        imageResult.drawable.toBitmap().writeTo(out)
                    }

                    continuation.resume(Result.success(Unit))
                } catch (e: Exception) {
                    continuation.resume(Result.failure(e))
                }
            }
            is ErrorResult -> Result.failure(imageResult.throwable)
        }
    }
}



package dev.leonlatsch.photok.imageloading.data

import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import dev.leonlatsch.photok.imageloading.domain.ImageStorage
import dev.leonlatsch.photok.other.extensions.writeTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ImageStorageImpl @Inject constructor(
    private val imageLoader: ImageLoader,
) : ImageStorage {

    /**
     * Executes an [imageRequest] and writes its result to the desired [outputStream].
     */
    override suspend fun execAndWrite(
        imageRequest: ImageRequest,
        outputStream: OutputStream?,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        outputStream ?: return@withContext Result.failure(Exception("stream is null"))

        when (val imageResult = imageLoader.execute(imageRequest)) {
            is SuccessResult -> suspendCoroutine { continuation ->
                try {
                    outputStream.use { out ->
                        imageResult.drawable.toBitmap().writeTo(out)
                    }

                    continuation.resume(Result.success(Unit))
                } catch (e: Exception) {
                    continuation.resume(Result.failure(e))
                }
            }
            is ErrorResult -> Result.failure(imageResult.throwable)
        }
    }
}

