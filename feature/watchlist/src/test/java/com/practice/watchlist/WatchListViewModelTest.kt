package com.practice.watchlist

import app.cash.turbine.test
import com.practice.domain.model.WatchlistMovie
import com.practice.domain.usecase.GetWatchlistUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getWatchlistUseCase = mockk<GetWatchlistUseCase>()

    @Before
    fun setup() { Dispatchers.setMain(testDispatcher) }

    @After
    fun teardown() { Dispatchers.resetMain() }

    @Test
    fun `watchlist emits items from use case`() = runTest {
        val movies = listOf(
            WatchlistMovie(id = 1, title = "Movie 1", posterPath = null, backdropPath = null,
                releaseDate = "2023", voteAverage = 8.0, runtime = 120, genre = "Action")
        )
        every { getWatchlistUseCase() } returns flowOf(movies)

        val vm = WatchListViewModel(getWatchlistUseCase)

        vm.watchlist.test {
            skipItems(1) // skip initial emptyList() from stateIn
            assertEquals(movies, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `watchlist starts empty`() = runTest {
        every { getWatchlistUseCase() } returns flowOf(emptyList())

        val vm = WatchListViewModel(getWatchlistUseCase)

        vm.watchlist.test {
            // stateIn initial value is emptyList(); flowOf(emptyList()) is equal so StateFlow deduplicates
            assertEquals(emptyList<WatchlistMovie>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
