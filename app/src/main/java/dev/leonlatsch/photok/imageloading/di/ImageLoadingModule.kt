


package dev.leonlatsch.photok.imageloading.di

import android.content.Context
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.imageloading.compose.EncryptedImageFetcherFactory
import dev.leonlatsch.photok.imageloading.data.ImageStorageImpl
import dev.leonlatsch.photok.imageloading.domain.ImageStorage

@Module
@InstallIn(SingletonComponent::class)
object ImageLoadingModule {

    @Provides
    @EncryptedImageLoader
    fun provideEncryptedImageLoader(
        @ApplicationContext context: Context,
        encryptedImageFetcherFactory: EncryptedImageFetcherFactory
    ): ImageLoader = ImageLoader.Builder(context)
        .components {
            add(encryptedImageFetcherFactory)
        }
        .diskCachePolicy(CachePolicy.DISABLED)
        .diskCache(null)
        .memoryCachePolicy(CachePolicy.READ_ONLY)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.25)
                .build()
        }
        .apply {
            if (BuildConfig.DEBUG) {
                logger(DebugLogger())
            }
        }
        .build()

    @Provides
    fun provideDefaultImageLoader(
        @ApplicationContext context: Context,
    ): ImageLoader = ImageLoader.Builder(context)
        .components { add(VideoFrameDecoder.Factory()) }
        .diskCachePolicy(CachePolicy.DISABLED)
        .diskCache(null)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .memoryCache(null)
        .build()
}

@Module
@InstallIn(SingletonComponent::class)
interface ImageLoadingBindingModule {
    @Binds
    fun bindImageStorage(
        impl: ImageStorageImpl
    ): ImageStorage
}

package dev.leonlatsch.photok.imageloading.di

import android.content.Context
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.imageloading.compose.EncryptedImageFetcherFactory
import dev.leonlatsch.photok.imageloading.data.ImageStorageImpl
import dev.leonlatsch.photok.imageloading.domain.ImageStorage

@Module
@InstallIn(SingletonComponent::class)
object ImageLoadingModule {

    @Provides
    @EncryptedImageLoader
    fun provideEncryptedImageLoader(
        @ApplicationContext context: Context,
        encryptedImageFetcherFactory: EncryptedImageFetcherFactory
    ): ImageLoader = ImageLoader.Builder(context)
        .components {
            add(encryptedImageFetcherFactory)
        }
        .diskCachePolicy(CachePolicy.DISABLED)
        .diskCache(null)
        .memoryCachePolicy(CachePolicy.READ_ONLY)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.25)
                .build()
        }
        .apply {
            if (BuildConfig.DEBUG) {
                logger(DebugLogger())
            }
        }
        .build()

    @Provides
    fun provideDefaultImageLoader(
        @ApplicationContext context: Context,
    ): ImageLoader = ImageLoader.Builder(context)
        .components { add(VideoFrameDecoder.Factory()) }
        .diskCachePolicy(CachePolicy.DISABLED)
        .diskCache(null)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .memoryCache(null)
        .build()
}

@Module
@InstallIn(SingletonComponent::class)
interface ImageLoadingBindingModule {
    @Binds
    fun bindImageStorage(
        impl: ImageStorageImpl
    ): ImageStorage
}