/*
 *   Copyright 2020-2026 Leon Latsch
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

package dev.leonlatsch.photok.vaults.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.leonlatsch.photok.model.database.PhotokDatabase
import dev.leonlatsch.photok.vaults.data.VaultRepositoryImpl
import dev.leonlatsch.photok.vaults.domain.VaultRepository

@InstallIn(SingletonComponent::class)
@Module
class VaultModule {
    @Provides
    fun provideVaultDao(database: PhotokDatabase) = database.getVaultDao()
}

@InstallIn(SingletonComponent::class)
@Module
interface VaultBindingModule {

    @Binds
    fun bindVaultRepository(impl: VaultRepositoryImpl): VaultRepository
}