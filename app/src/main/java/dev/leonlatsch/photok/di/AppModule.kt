/*
 *   Copyright 2020 Leon Latsch
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
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.model.database.PhotokDatabase
import dev.leonlatsch.photok.model.database.PhotokDatabase.Companion.DATABASE_NAME
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.settings.Config
import javax.inject.Singleton

/**
 * Hilt Module for [ApplicationComponent].
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePhotokDatabase(@ApplicationContext app: Context) = Room.databaseBuilder(
        app,
        PhotokDatabase::class.java,
        DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun providePhotoDao(database: PhotokDatabase) = database.getPhotoDao()

    @Provides
    @Singleton
    fun providePasswordDao(database: PhotokDatabase) = database.getPasswordDao()

    @Provides
    @Singleton
    fun providePrefManager(@ApplicationContext app: Context) = Config(app)

    @Provides
    @Singleton
    fun providePasswordManager() = EncryptionManager()
}