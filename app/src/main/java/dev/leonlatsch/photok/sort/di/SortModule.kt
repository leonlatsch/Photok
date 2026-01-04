


package dev.leonlatsch.photok.sort.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.leonlatsch.photok.sort.data.SortRepositoryImpl
import dev.leonlatsch.photok.sort.domain.SortRepository
import dev.leonlatsch.photok.model.database.PhotokDatabase

@Module
@InstallIn(SingletonComponent::class)
class SortModule {

    @Provides
    fun provideSortDao(database: PhotokDatabase) = database.getSortDao()
}

@Module
@InstallIn(SingletonComponent::class)
interface SortBindingModule {

    @Binds
    fun bindSortRepository(impl: SortRepositoryImpl): SortRepository
}


package dev.leonlatsch.photok.sort.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.leonlatsch.photok.sort.data.SortRepositoryImpl
import dev.leonlatsch.photok.sort.domain.SortRepository
import dev.leonlatsch.photok.model.database.PhotokDatabase

@Module
@InstallIn(SingletonComponent::class)
class SortModule {

    @Provides
    fun provideSortDao(database: PhotokDatabase) = database.getSortDao()
}

@Module
@InstallIn(SingletonComponent::class)
interface SortBindingModule {

    @Binds
    fun bindSortRepository(impl: SortRepositoryImpl): SortRepository
}
