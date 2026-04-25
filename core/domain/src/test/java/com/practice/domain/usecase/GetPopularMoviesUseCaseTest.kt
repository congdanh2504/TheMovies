package com.practice.domain.usecase

import com.practice.domain.model.Movie
import com.practice.domain.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetPopularMoviesUseCaseTest {

    private val repository = mockk<MovieRepository>()
    private val useCase = GetPopularMoviesUseCase(repository)

    private val sampleMovies = listOf(
        Movie(1, "Movie 1", "Overview", "poster", "backdrop", "2023-01-01", 8.0, 100)
    )

    @Test
    fun `returns Success from repository`() = runTest {
        coEvery { repository.getPopularMovies() } returns Result.success(sampleMovies)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(sampleMovies, result.getOrNull())
        coVerify(exactly = 1) { repository.getPopularMovies() }
    }

    @Test
    fun `returns Failure when repository throws`() = runTest {
        val error = RuntimeException("Network error")
        coEvery { repository.getPopularMovies() } returns Result.failure(error)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
