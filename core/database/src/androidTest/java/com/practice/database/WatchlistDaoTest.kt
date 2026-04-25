package com.practice.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.practice.database.dao.WatchlistDao
import com.practice.database.entity.WatchlistEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WatchlistDaoTest {

    private lateinit var db: TheMoviesDatabase
    private lateinit var dao: WatchlistDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TheMoviesDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.watchlistDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun entity(id: Int = 1) = WatchlistEntity(
        movieId = id, title = "Movie $id", posterPath = null, backdropPath = null,
        releaseDate = "2023-01-01", voteAverage = 8.0, runtime = 120, genre = "Action"
    )

    @Test
    fun insertAndGetAll() = runTest {
        dao.insert(entity(1))
        dao.insert(entity(2))

        dao.getAll().test {
            val items = awaitItem()
            assertEquals(2, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun existsById_trueAfterInsert() = runTest {
        dao.insert(entity(1))

        dao.existsById(1).test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun existsById_falseBeforeInsert() = runTest {
        dao.existsById(99).test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteById_removesItem() = runTest {
        dao.insert(entity(1))
        dao.deleteById(1)

        dao.existsById(1).test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateAndGetRating() = runTest {
        dao.insert(entity(1))
        dao.updateRating(1, 7.5f)

        dao.getRating(1).test {
            assertEquals(7.5f, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getRating_nullBeforeUpdate() = runTest {
        dao.insert(entity(1))

        dao.getRating(1).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
