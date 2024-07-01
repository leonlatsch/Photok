/*
 *   Copyright 2020-2024 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.leonlatsch.photok.imageloading.di

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.CachePolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.leonlatsch.photok.imageloading.compose.EncryptedImageFetcherFactory

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
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .diskCachePolicy(CachePolicy.DISABLED)
        .diskCache(null)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .memoryCache(null)
        .build()
}