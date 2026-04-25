package com.practice.home

import app.cash.turbine.test
import com.practice.datastore.UserPreferences
import com.practice.datastore.UserPreferencesRepository
import com.practice.domain.model.Movie
import com.practice.domain.usecase.GetNowPlayingMoviesUseCase
import com.practice.domain.usecase.GetPopularMoviesUseCase
import com.practice.domain.usecase.GetTopRatedMoviesUseCase
import com.practice.domain.usecase.GetUpcomingMoviesUseCase
import io.mockk.coEvery
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getUpcoming = mockk<GetUpcomingMoviesUseCase>()
    private val getNowPlaying = mockk<GetNowPlayingMoviesUseCase>()
    private val getTopRated = mockk<GetTopRatedMoviesUseCase>()
    private val getPopular = mockk<GetPopularMoviesUseCase>()
    private val userPrefsRepo = mockk<UserPreferencesRepository>()

    private val sampleMovies = listOf(
        Movie(1, "Movie 1", "Overview", "poster", "backdrop", "2023-01-01", 8.0, 100)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { userPrefsRepo.preferences } returns flowOf(UserPreferences(selectedHomeTab = 0))
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        coEvery { getUpcoming() } coAnswers { kotlinx.coroutines.delay(100); Result.success(sampleMovies) }
        coEvery { getNowPlaying() } coAnswers { kotlinx.coroutines.delay(100); Result.success(sampleMovies) }
        coEvery { getTopRated() } coAnswers { kotlinx.coroutines.delay(100); Result.success(sampleMovies) }
        coEvery { getPopular() } coAnswers { kotlinx.coroutines.delay(100); Result.success(sampleMovies) }

        val vm = createViewModel()
        vm.uiState.test {
            assertTrue(awaitItem() is HomeUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `all use cases succeed - emits Success`() = runTest {
        coEvery { getUpcoming() } returns Result.success(sampleMovies)
        coEvery { getNowPlaying() } returns Result.success(sampleMovies)
        coEvery { getTopRated() } returns Result.success(sampleMovies)
        coEvery { getPopular() } returns Result.success(sampleMovies)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is HomeUiState.Success)
        assertEquals(sampleMovies, (state as HomeUiState.Success).nowPlayingMovies)
    }

    @Test
    fun `all use cases fail - emits Error`() = runTest {
        val error = RuntimeException("Network error")
        coEvery { getUpcoming() } returns Result.failure(error)
        coEvery { getNowPlaying() } returns Result.failure(error)
        coEvery { getTopRated() } returns Result.failure(error)
        coEvery { getPopular() } returns Result.failure(error)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertEquals("Network error", (state as HomeUiState.Error).message)
    }

    @Test
    fun `partial failure - emits Success with empty lists for failed categories`() = runTest {
        coEvery { getUpcoming() } returns Result.failure(RuntimeException("fail"))
        coEvery { getNowPlaying() } returns Result.success(sampleMovies)
        coEvery { getTopRated() } returns Result.success(sampleMovies)
        coEvery { getPopular() } returns Result.success(sampleMovies)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as HomeUiState.Success
        assertTrue(state.upcomingMovies.isEmpty())
        assertEquals(sampleMovies, state.nowPlayingMovies)
    }

    private fun createViewModel() = HomeViewModel(
        getUpcomingMoviesUseCase = getUpcoming,
        getNowPlayingMoviesUseCase = getNowPlaying,
        getTopRatedMoviesUseCase = getTopRated,
        getPopularMoviesUseCase = getPopular,
        userPreferencesRepository = userPrefsRepo
    )
}
