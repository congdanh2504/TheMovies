package com.practice.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.practice.database.dao.WatchlistDao
import com.practice.database.entity.WatchlistEntity

@Database(entities = [WatchlistEntity::class], version = 1, exportSchema = false)
abstract class TheMoviesDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
}
