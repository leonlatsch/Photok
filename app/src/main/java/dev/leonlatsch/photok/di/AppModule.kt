/*
 *   Copyright 2020-2021 Leon Latsch
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

package dev.leonlatsch.photok.di

import android.content.Context
import android.content.res.Resources
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.gallery.ui.importing.SharedUrisStore
import dev.leonlatsch.photok.model.database.DATABASE_NAME
import dev.leonlatsch.photok.model.database.PhotokDatabase
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.settings.data.Config
import timber.log.Timber
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Singleton

/**
 * Hilt Module for [SingletonComponent].
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePhotokDatabase(@ApplicationContext app: Context) = Room.databaseBuilder(
        app,
        PhotokDatabase::class.java,
        DATABASE_NAME
    ).apply {
        if (BuildConfig.DEBUG) {
            setQueryCallback(object : RoomDatabase.QueryCallback {
                override fun onQuery(
                    sqlQuery: String,
                    bindArgs: List<Any?>
                ) {
                    Timber.d("SQL: $sqlQuery | args: $bindArgs")
                }
            }, Executors.newSingleThreadExecutor())
        } else {
            this
        }
    }.build()

    @Provides
    @Singleton
    fun providePhotoDao(database: PhotokDatabase) = database.getPhotoDao()

    @Provides
    @Singleton
    fun provideAlbumDao(database: PhotokDatabase) = database.getAlbumDao()

    @Provides
    @Singleton
    fun provideConfig(@ApplicationContext app: Context) = Config(app)

    @Provides
    @Singleton
    fun provideSharedUrisStore() = SharedUrisStore()

    @Provides
    fun provideResources(@ApplicationContext context: Context): Resources = context.resources

    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
}