package dev.leonlatsch.photok.security.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.security.EncryptionManagerImpl
import dev.leonlatsch.photok.security.biometric.BiometricUnlock
import dev.leonlatsch.photok.security.biometric.BiometricUnlockImpl
import dev.leonlatsch.photok.security.migration.LegacyEncryptionManager
import dev.leonlatsch.photok.security.migration.LegacyEncryptionManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface EncryptionModule {

    @Binds
    @Singleton
    fun bindEncryptionManager(impl: EncryptionManagerImpl): EncryptionManager

    @Binds
    @Singleton
    @LegacyEncryptionManager
    fun bindLegacyEncryptionManager(impl: LegacyEncryptionManagerImpl): EncryptionManager

    @Binds
    @Singleton
    fun bindBiometricUnlock(impl: BiometricUnlockImpl): BiometricUnlock
}