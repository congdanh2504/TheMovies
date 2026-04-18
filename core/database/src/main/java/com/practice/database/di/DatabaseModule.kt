package com.practice.database.di

import android.content.Context
import androidx.room.Room
import com.practice.database.TheMoviesDatabase
import com.practice.database.dao.WatchlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TheMoviesDatabase =
        Room.databaseBuilder(context, TheMoviesDatabase::class.java, "themovies.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideWatchlistDao(db: TheMoviesDatabase): WatchlistDao = db.watchlistDao()
}
