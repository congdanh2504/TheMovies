package com.practice.data.repository

import com.practice.network.TheMoviesApi
import com.practice.network.model.NetworkMovie
import com.practice.network.model.PageResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieRepositoryImplTest {

    private val api = mockk<TheMoviesApi>()
    private val repository = MovieRepositoryImpl(api)

    private val fakeResponse = PageResponse(
        page = 1,
        results = listOf(
            NetworkMovie(
                id = 1, title = "Movie 1", overview = "Overview",
                posterPath = "/poster.jpg", backdropPath = "/backdrop.jpg",
                releaseDate = "2023-01-01", voteAverage = 8.0, voteCount = 100
            )
        ),
        totalPages = 1,
        totalResults = 1
    )

    @Test
    fun `getPopularMovies - success returns mapped movies`() = runTest {
        coEvery { api.getPopular() } returns fakeResponse

        val result = repository.getPopularMovies()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isNotEmpty())
    }

    @Test
    fun `getPopularMovies - api exception returns failure`() = runTest {
        coEvery { api.getPopular() } throws RuntimeException("Timeout")

        val result = repository.getPopularMovies()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message == "Timeout")
    }

    @Test
    fun `getNowPlayingMovies - success`() = runTest {
        coEvery { api.getNowPlaying() } returns fakeResponse
        assertTrue(repository.getNowPlayingMovies().isSuccess)
    }

    @Test
    fun `getTopRatedMovies - success`() = runTest {
        coEvery { api.getTopRated() } returns fakeResponse
        assertTrue(repository.getTopRatedMovies().isSuccess)
    }

    @Test
    fun `getUpcomingMovies - success`() = runTest {
        coEvery { api.getUpcoming() } returns fakeResponse
        assertTrue(repository.getUpcomingMovies().isSuccess)
    }
}
