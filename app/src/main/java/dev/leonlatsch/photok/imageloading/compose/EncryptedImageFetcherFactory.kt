


package dev.leonlatsch.photok.imageloading.compose

import android.content.Context
import android.content.res.Resources
import android.view.WindowManager
import coil.ImageLoader
import coil.fetch.Fetcher
import coil.request.Options
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import javax.inject.Inject

class EncryptedImageFetcherFactory @Inject constructor(
    private val encryptedStorageManager: EncryptedStorageManager,
    @ApplicationContext private val context: Context,
) : Fetcher.Factory<EncryptedImageRequestData> {
    override fun create(data: EncryptedImageRequestData, options: Options, imageLoader: ImageLoader): Fetcher =
        EncryptedImageFetcher(
            encryptedStorageManager = encryptedStorageManager,
            requestData = data,
            context = context,
        )

}

package dev.leonlatsch.photok.imageloading.compose

import android.content.Context
import android.content.res.Resources
import android.view.WindowManager
import coil.ImageLoader
import coil.fetch.Fetcher
import coil.request.Options
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import javax.inject.Inject

class EncryptedImageFetcherFactory @Inject constructor(
    private val encryptedStorageManager: EncryptedStorageManager,
    @ApplicationContext private val context: Context,
) : Fetcher.Factory<EncryptedImageRequestData> {
    override fun create(data: EncryptedImageRequestData, options: Options, imageLoader: ImageLoader): Fetcher =
        EncryptedImageFetcher(
            encryptedStorageManager = encryptedStorageManager,
            requestData = data,
            context = context,
        )

}