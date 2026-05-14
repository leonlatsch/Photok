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

package dev.leonlatsch.photok.encryption.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.leonlatsch.photok.encryption.data.SessionRepositoryImpl
import dev.leonlatsch.photok.encryption.data.VaultProtectionRepositoryImpl
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultProtectionRepository
import dev.leonlatsch.photok.encryption.domain.crypto.CbcCryptoEngine
import dev.leonlatsch.photok.encryption.domain.crypto.CryptoEngine
import dev.leonlatsch.photok.encryption.domain.handlers.BiometricVaultProtectionHandler
import dev.leonlatsch.photok.encryption.domain.handlers.PasswordVaultProtectionHandler
import dev.leonlatsch.photok.encryption.domain.handlers.VaultProtectionHandler
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.model.database.PhotokDatabase

@Module
@InstallIn(SingletonComponent::class)
interface EncryptionBindingModule {

    @Binds
    fun bindVaultProtectionRepository(impl: VaultProtectionRepositoryImpl): VaultProtectionRepository

    @Binds
    fun bindPasswordUnlocker(impl: PasswordVaultProtectionHandler): VaultProtectionHandler<UnlockRequest.Password, CreateRequest.Password>

    @Binds
    fun bindBiometricUnlocker(impl: BiometricVaultProtectionHandler): VaultProtectionHandler<UnlockRequest.Biometric, CreateRequest.Biometric>

    @Binds
    fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    fun bindCryptoEngine(impl: CbcCryptoEngine): CryptoEngine
}

@Module
@InstallIn(SingletonComponent::class)
class EncryptionModule {

    @Provides
    fun provideVaultProtectionDao(database: PhotokDatabase) = database.getVaultProtectionDao()
}