


package dev.leonlatsch.photok.gallery.albums.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.leonlatsch.photok.gallery.albums.data.AlbumRepositoryImpl
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository

@Module
@InstallIn(SingletonComponent::class)
interface AlbumsModule {
    @Binds
    fun bindAlbumsRepository(impl: AlbumRepositoryImpl): AlbumRepository
}

package dev.leonlatsch.photok.gallery.albums.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.leonlatsch.photok.gallery.albums.data.AlbumRepositoryImpl
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository

@Module
@InstallIn(SingletonComponent::class)
interface AlbumsModule {
    @Binds
    fun bindAlbumsRepository(impl: AlbumRepositoryImpl): AlbumRepository
}