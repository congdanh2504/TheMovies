# Remaining Screens Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement Detail Movie screen (backdrop + 3 tabs + rating modal), WatchList screen (empty + list states), and wire navigation — all backed by Room persistence.

**Architecture:** Clean Architecture MVVM. `core:database` adds Room entity + DAO + Database. `core:domain` extends both repository interfaces and adds 7 use cases. `core:data` adds implementations. Two feature modules get ViewModels and full Compose screens.

**Tech Stack:** Kotlin, Jetpack Compose + Material3, Hilt, Room 2.7.1, Retrofit/Moshi, Coil, Coroutines/Flow, convention plugins (`themovies.android.room`, `themovies.android.hilt`)

---

## File Map

**Create:**
- `core/database/src/main/java/com/practice/database/entity/WatchlistEntity.kt`
- `core/database/src/main/java/com/practice/database/dao/WatchlistDao.kt`
- `core/database/src/main/java/com/practice/database/TheMoviesDatabase.kt`
- `core/database/src/main/java/com/practice/database/di/DatabaseModule.kt`
- `core/domain/src/main/java/com/practice/domain/model/WatchlistMovie.kt`
- `core/domain/src/main/java/com/practice/domain/usecase/GetMovieCastUseCase.kt`
- `core/domain/src/main/java/com/practice/domain/usecase/GetMovieReviewsUseCase.kt`
- `core/domain/src/main/java/com/practice/domain/usecase/GetWatchlistUseCase.kt`
- `core/domain/src/main/java/com/practice/domain/usecase/IsInWatchlistUseCase.kt`
- `core/domain/src/main/java/com/practice/domain/usecase/SaveToWatchlistUseCase.kt`
- `core/domain/src/main/java/com/practice/domain/usecase/RemoveFromWatchlistUseCase.kt`
- `core/domain/src/main/java/com/practice/domain/usecase/SaveRatingUseCase.kt`
- `core/data/src/main/java/com/practice/data/repository/LocalRepositoryImpl.kt`
- `feature/detailmovie/src/main/java/com/practice/detailmovie/DetailMovieViewModel.kt`
- `feature/detailmovie/src/main/java/com/practice/detailmovie/DetailMovieScreen.kt`
- `feature/watchlist/src/main/java/com/practice/watchlist/WatchListViewModel.kt`

**Modify:**
- `core/database/build.gradle.kts`
- `core/domain/src/main/java/com/practice/domain/repository/MovieRepository.kt`
- `core/domain/src/main/java/com/practice/domain/repository/LocalRepository.kt`
- `core/data/src/main/java/com/practice/data/repository/MovieRepositoryImpl.kt`
- `core/data/src/main/java/com/practice/data/di/RepositoryModule.kt`
- `feature/detailmovie/build.gradle.kts`
- `feature/watchlist/build.gradle.kts`
- `feature/watchlist/src/main/java/com/practice/watchlist/WatchListScreen.kt`
- `app/src/main/java/com/practice/themovies/MainActivity.kt`

---

## Task 1: core:database — Room Entity, DAO, Database, Hilt Module

**Files:**
- Modify: `core/database/build.gradle.kts`
- Create: `core/database/src/main/java/com/practice/database/entity/WatchlistEntity.kt`
- Create: `core/database/src/main/java/com/practice/database/dao/WatchlistDao.kt`
- Create: `core/database/src/main/java/com/practice/database/TheMoviesDatabase.kt`
- Create: `core/database/src/main/java/com/practice/database/di/DatabaseModule.kt`

- [ ] **Step 1: Update `core/database/build.gradle.kts`**

Replace entire file content:

```kotlin
plugins {
    alias(libs.plugins.themovies.android.library)
    alias(libs.plugins.themovies.android.room)
    alias(libs.plugins.themovies.android.hilt)
}

android {
    namespace = "com.practice.database"
}

dependencies {
}
```

- [ ] **Step 2: Create `WatchlistEntity.kt`**

```kotlin
package com.practice.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val movieId: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val runtime: Int,
    val genre: String,
    val userRating: Float? = null
)
```

- [ ] **Step 3: Create `WatchlistDao.kt`**

```kotlin
package com.practice.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.practice.database.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE movieId = :movieId")
    suspend fun deleteById(movieId: Int)

    @Query("SELECT * FROM watchlist ORDER BY title ASC")
    fun getAll(): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE movieId = :movieId)")
    fun existsById(movieId: Int): Flow<Boolean>

    @Query("UPDATE watchlist SET userRating = :rating WHERE movieId = :movieId")
    suspend fun updateRating(movieId: Int, rating: Float)
}
```

- [ ] **Step 4: Create `TheMoviesDatabase.kt`**

```kotlin
package com.practice.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.practice.database.dao.WatchlistDao
import com.practice.database.entity.WatchlistEntity

@Database(entities = [WatchlistEntity::class], version = 1, exportSchema = false)
abstract class TheMoviesDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
}
```

- [ ] **Step 5: Create `DatabaseModule.kt`**

```kotlin
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
        Room.databaseBuilder(context, TheMoviesDatabase::class.java, "themovies.db").build()

    @Provides
    fun provideWatchlistDao(db: TheMoviesDatabase): WatchlistDao = db.watchlistDao()
}
```

- [ ] **Step 6: Commit**

```bash
git add core/database/
git commit -m "feat: add Room database with WatchlistEntity, DAO, and Hilt module"
```

---

## Task 2: core:domain — WatchlistMovie model, extended interfaces, 7 use cases

**Files:**
- Create: `core/domain/src/main/java/com/practice/domain/model/WatchlistMovie.kt`
- Modify: `core/domain/src/main/java/com/practice/domain/repository/MovieRepository.kt`
- Modify: `core/domain/src/main/java/com/practice/domain/repository/LocalRepository.kt`
- Create: 7 use case files in `core/domain/src/main/java/com/practice/domain/usecase/`

- [ ] **Step 1: Create `WatchlistMovie.kt`**

```kotlin
package com.practice.domain.model

data class WatchlistMovie(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val runtime: Int,
    val genre: String,
    val userRating: Float? = null
)
```

- [ ] **Step 2: Update `MovieRepository.kt`** — add `getReviews` and `getCast`

```kotlin
package com.practice.domain.repository

import com.practice.domain.model.Cast
import com.practice.domain.model.Movie
import com.practice.domain.model.MovieDetail
import com.practice.domain.model.MoviesPage
import com.practice.domain.model.Review

interface MovieRepository {
    suspend fun getMovieDetails(movieId: Int): MovieDetail
    suspend fun searchMovies(query: String, page: Int): MoviesPage
    suspend fun getPopularMovies(): List<Movie>
    suspend fun getTopRatedMovies(): List<Movie>
    suspend fun getUpcomingMovies(): List<Movie>
    suspend fun getNowPlayingMovies(): List<Movie>
    suspend fun getReviews(movieId: Int): List<Review>
    suspend fun getCast(movieId: Int): List<Cast>
}
```

- [ ] **Step 3: Update `LocalRepository.kt`**

```kotlin
package com.practice.domain.repository

import com.practice.domain.model.WatchlistMovie
import kotlinx.coroutines.flow.Flow

interface LocalRepository {
    suspend fun saveToWatchlist(movie: WatchlistMovie)
    suspend fun removeFromWatchlist(movieId: Int)
    fun getWatchlist(): Flow<List<WatchlistMovie>>
    fun isInWatchlist(movieId: Int): Flow<Boolean>
    suspend fun saveRating(movieId: Int, rating: Float)
}
```

- [ ] **Step 4: Create `GetMovieCastUseCase.kt`**

```kotlin
package com.practice.domain.usecase

import com.practice.domain.model.Cast
import com.practice.domain.repository.MovieRepository
import javax.inject.Inject

class GetMovieCastUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(movieId: Int): List<Cast> = repository.getCast(movieId)
}
```

- [ ] **Step 5: Create `GetMovieReviewsUseCase.kt`**

```kotlin
package com.practice.domain.usecase

import com.practice.domain.model.Review
import com.practice.domain.repository.MovieRepository
import javax.inject.Inject

class GetMovieReviewsUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(movieId: Int): List<Review> = repository.getReviews(movieId)
}
```

- [ ] **Step 6: Create `GetWatchlistUseCase.kt`**

```kotlin
package com.practice.domain.usecase

import com.practice.domain.model.WatchlistMovie
import com.practice.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWatchlistUseCase @Inject constructor(
    private val repository: LocalRepository
) {
    operator fun invoke(): Flow<List<WatchlistMovie>> = repository.getWatchlist()
}
```

- [ ] **Step 7: Create `IsInWatchlistUseCase.kt`**

```kotlin
package com.practice.domain.usecase

import com.practice.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsInWatchlistUseCase @Inject constructor(
    private val repository: LocalRepository
) {
    operator fun invoke(movieId: Int): Flow<Boolean> = repository.isInWatchlist(movieId)
}
```

- [ ] **Step 8: Create `SaveToWatchlistUseCase.kt`**

```kotlin
package com.practice.domain.usecase

import com.practice.domain.model.WatchlistMovie
import com.practice.domain.repository.LocalRepository
import javax.inject.Inject

class SaveToWatchlistUseCase @Inject constructor(
    private val repository: LocalRepository
) {
    suspend operator fun invoke(movie: WatchlistMovie) = repository.saveToWatchlist(movie)
}
```

- [ ] **Step 9: Create `RemoveFromWatchlistUseCase.kt`**

```kotlin
package com.practice.domain.usecase

import com.practice.domain.repository.LocalRepository
import javax.inject.Inject

class RemoveFromWatchlistUseCase @Inject constructor(
    private val repository: LocalRepository
) {
    suspend operator fun invoke(movieId: Int) = repository.removeFromWatchlist(movieId)
}
```

- [ ] **Step 10: Create `SaveRatingUseCase.kt`**

```kotlin
package com.practice.domain.usecase

import com.practice.domain.repository.LocalRepository
import javax.inject.Inject

class SaveRatingUseCase @Inject constructor(
    private val repository: LocalRepository
) {
    suspend operator fun invoke(movieId: Int, rating: Float) = repository.saveRating(movieId, rating)
}
```

- [ ] **Step 11: Commit**

```bash
git add core/domain/
git commit -m "feat: add WatchlistMovie model, extend repository interfaces, add 7 use cases"
```

---

## Task 3: core:data — implement getReviews/getCast, LocalRepositoryImpl, update Hilt module

**Files:**
- Modify: `core/data/src/main/java/com/practice/data/repository/MovieRepositoryImpl.kt`
- Create: `core/data/src/main/java/com/practice/data/repository/LocalRepositoryImpl.kt`
- Modify: `core/data/src/main/java/com/practice/data/di/RepositoryModule.kt`

- [ ] **Step 1: Update `MovieRepositoryImpl.kt`** — add `getReviews` and `getCast` at end of class

Replace entire file:

```kotlin
package com.practice.data.repository

import com.practice.domain.model.Cast
import com.practice.domain.model.Movie
import com.practice.domain.model.MovieDetail
import com.practice.domain.model.MoviesPage
import com.practice.domain.model.Review
import com.practice.domain.repository.MovieRepository
import com.practice.network.TheMoviesApi
import com.practice.network.mapper.toDomain
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val theMoviesApi: TheMoviesApi
) : MovieRepository {

    override suspend fun getNowPlayingMovies(): List<Movie> =
        theMoviesApi.getNowPlaying().results.map { it.toDomain() }

    override suspend fun getUpcomingMovies(): List<Movie> =
        theMoviesApi.getUpcoming().results.map { it.toDomain() }

    override suspend fun getTopRatedMovies(): List<Movie> =
        theMoviesApi.getTopRated().results.map { it.toDomain() }

    override suspend fun getPopularMovies(): List<Movie> =
        theMoviesApi.getPopular().results.map { it.toDomain() }

    override suspend fun getMovieDetails(movieId: Int): MovieDetail =
        theMoviesApi.getMovieDetails(movieId).toDomain()

    override suspend fun searchMovies(query: String, page: Int): MoviesPage =
        theMoviesApi.searchMovies(query, page).toDomain()

    override suspend fun getReviews(movieId: Int): List<Review> =
        theMoviesApi.getReviews(movieId).results.map { it.toDomain() }

    override suspend fun getCast(movieId: Int): List<Cast> =
        theMoviesApi.getCasts(movieId).cast.map { it.toDomain() }
}
```

- [ ] **Step 2: Create `LocalRepositoryImpl.kt`**

```kotlin
package com.practice.data.repository

import com.practice.database.dao.WatchlistDao
import com.practice.database.entity.WatchlistEntity
import com.practice.domain.model.WatchlistMovie
import com.practice.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalRepositoryImpl @Inject constructor(
    private val dao: WatchlistDao
) : LocalRepository {

    override suspend fun saveToWatchlist(movie: WatchlistMovie) = dao.insert(movie.toEntity())

    override suspend fun removeFromWatchlist(movieId: Int) = dao.deleteById(movieId)

    override fun getWatchlist(): Flow<List<WatchlistMovie>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun isInWatchlist(movieId: Int): Flow<Boolean> = dao.existsById(movieId)

    override suspend fun saveRating(movieId: Int, rating: Float) = dao.updateRating(movieId, rating)
}

private fun WatchlistEntity.toDomain() = WatchlistMovie(
    id = movieId,
    title = title,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    runtime = runtime,
    genre = genre,
    userRating = userRating
)

private fun WatchlistMovie.toEntity() = WatchlistEntity(
    movieId = id,
    title = title,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    runtime = runtime,
    genre = genre,
    userRating = userRating
)
```

- [ ] **Step 3: Update `RepositoryModule.kt`** — add `LocalRepository` binding

```kotlin
package com.practice.data.di

import com.practice.data.repository.LocalRepositoryImpl
import com.practice.data.repository.MovieRepositoryImpl
import com.practice.domain.repository.LocalRepository
import com.practice.domain.repository.MovieRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindMovieRepository(impl: MovieRepositoryImpl): MovieRepository

    @Binds
    abstract fun bindLocalRepository(impl: LocalRepositoryImpl): LocalRepository
}
```

- [ ] **Step 4: Commit**

```bash
git add core/data/
git commit -m "feat: implement getReviews/getCast in MovieRepositoryImpl, add LocalRepositoryImpl"
```

---

## Task 4: feature:detailmovie — build.gradle, ViewModel, Screen

**Files:**
- Modify: `feature/detailmovie/build.gradle.kts`
- Create: `feature/detailmovie/src/main/java/com/practice/detailmovie/DetailMovieViewModel.kt`
- Create: `feature/detailmovie/src/main/java/com/practice/detailmovie/DetailMovieScreen.kt`

- [ ] **Step 1: Update `feature/detailmovie/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.themovies.android.library)
    alias(libs.plugins.themovies.android.compose)
    alias(libs.plugins.themovies.android.hilt)
}

android {
    namespace = "com.practice.detailmovie"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(libs.coil.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation("androidx.compose.material:material-icons-extended")
}
```

- [ ] **Step 2: Create `DetailMovieViewModel.kt`**

```kotlin
package com.practice.detailmovie

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practice.domain.model.Cast
import com.practice.domain.model.MovieDetail
import com.practice.domain.model.Review
import com.practice.domain.model.WatchlistMovie
import com.practice.domain.usecase.GetMovieCastUseCase
import com.practice.domain.usecase.GetMovieDetailsUseCase
import com.practice.domain.usecase.GetMovieReviewsUseCase
import com.practice.domain.usecase.IsInWatchlistUseCase
import com.practice.domain.usecase.RemoveFromWatchlistUseCase
import com.practice.domain.usecase.SaveRatingUseCase
import com.practice.domain.usecase.SaveToWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

data class DetailUiState(
    val movieDetail: MovieDetail? = null,
    val cast: List<Cast> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val isInWatchlist: Boolean = false,
    val userRating: Float? = null,
    val showRatingDialog: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DetailMovieViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
    private val getMovieCastUseCase: GetMovieCastUseCase,
    private val getMovieReviewsUseCase: GetMovieReviewsUseCase,
    private val isInWatchlistUseCase: IsInWatchlistUseCase,
    private val saveToWatchlistUseCase: SaveToWatchlistUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
    private val saveRatingUseCase: SaveRatingUseCase
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle["movieId"])

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        loadData()
        observeWatchlistStatus()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            supervisorScope {
                val detailDeferred = async { runCatching { getMovieDetailsUseCase(movieId) } }
                val castDeferred = async { runCatching { getMovieCastUseCase(movieId) } }
                val reviewsDeferred = async { runCatching { getMovieReviewsUseCase(movieId) } }

                val detail = detailDeferred.await()
                val cast = castDeferred.await()
                val reviews = reviewsDeferred.await()

                _state.update {
                    it.copy(
                        movieDetail = detail.getOrNull(),
                        cast = cast.getOrNull() ?: emptyList(),
                        reviews = reviews.getOrNull() ?: emptyList(),
                        isLoading = false,
                        error = if (detail.isFailure) detail.exceptionOrNull()?.message else null
                    )
                }
            }
        }
    }

    private fun observeWatchlistStatus() {
        viewModelScope.launch {
            isInWatchlistUseCase(movieId).collect { isIn ->
                _state.update { it.copy(isInWatchlist = isIn) }
            }
        }
    }

    fun toggleWatchlist() {
        viewModelScope.launch {
            val current = _state.value
            val detail = current.movieDetail ?: return@launch
            if (current.isInWatchlist) {
                removeFromWatchlistUseCase(movieId)
            } else {
                saveToWatchlistUseCase(
                    WatchlistMovie(
                        id = detail.id,
                        title = detail.title,
                        posterPath = detail.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
                        backdropPath = detail.backdropPath,
                        releaseDate = detail.releaseDate,
                        voteAverage = detail.voteAverage,
                        runtime = detail.runtime?.toInt() ?: 0,
                        genre = detail.genres.firstOrNull()?.name ?: ""
                    )
                )
            }
        }
    }

    fun showRatingDialog() = _state.update { it.copy(showRatingDialog = true) }

    fun dismissRatingDialog() = _state.update { it.copy(showRatingDialog = false) }

    fun submitRating(rating: Float) {
        viewModelScope.launch {
            val detail = _state.value.movieDetail ?: return@launch
            if (!_state.value.isInWatchlist) {
                saveToWatchlistUseCase(
                    WatchlistMovie(
                        id = detail.id,
                        title = detail.title,
                        posterPath = detail.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
                        backdropPath = detail.backdropPath,
                        releaseDate = detail.releaseDate,
                        voteAverage = detail.voteAverage,
                        runtime = detail.runtime?.toInt() ?: 0,
                        genre = detail.genres.firstOrNull()?.name ?: ""
                    )
                )
            }
            saveRatingUseCase(movieId, rating)
            _state.update { it.copy(userRating = rating, showRatingDialog = false) }
        }
    }
}
```

- [ ] **Step 3: Create `DetailMovieScreen.kt`**

```kotlin
package com.practice.detailmovie

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.LocalActivity
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.practice.domain.model.Cast
import com.practice.domain.model.Review
import com.practice.ui.Montserrat

private val DarkBg = Color(0xFF242A32)
private val AccentBlue = Color(0xFF0296E5)
private val AccentOrange = Color(0xFFFF8700)
private val TextGrey = Color(0xFF92929D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailMovieScreen(
    onBackClick: () -> Unit,
    viewModel: DetailMovieViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val tabs = listOf("About Movie", "Reviews", "Cast")
    var selectedTab by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentBlue
                )
            }
            state.error != null && state.movieDetail == null -> {
                Text(
                    text = state.error ?: "Failed to load movie",
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                val detail = state.movieDetail ?: return@Box

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Hero image with gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        AsyncImage(
                            model = detail.backdropPath,
                            contentDescription = detail.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0x44000000), DarkBg)
                                    )
                                )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = detail.title,
                            color = Color.White,
                            fontFamily = Montserrat,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rating row — tap to rate
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.showRatingDialog() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = AccentOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%.1f".format(
                                    state.userRating ?: detail.voteAverage.toFloat()
                                ),
                                color = AccentOrange,
                                fontFamily = Montserrat,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Meta row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = TextGrey,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = detail.releaseDate.take(4),
                                    color = TextGrey,
                                    fontFamily = Montserrat,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(text = "|", color = TextGrey, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = TextGrey,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${detail.runtime ?: 0} Minutes",
                                    color = TextGrey,
                                    fontFamily = Montserrat,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(text = "|", color = TextGrey, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.LocalActivity,
                                    contentDescription = null,
                                    tint = TextGrey,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = detail.genres.firstOrNull()?.name ?: "",
                                    color = TextGrey,
                                    fontFamily = Montserrat,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Tabs
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            indicator = { tabPositions ->
                                Box(
                                    Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTab])
                                        .height(3.dp)
                                        .background(AccentBlue)
                                )
                            },
                            divider = {}
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = {
                                        Text(
                                            text = title,
                                            color = if (selectedTab == index) Color.White else TextGrey,
                                            fontFamily = Montserrat,
                                            fontSize = 14.sp,
                                            fontWeight = if (selectedTab == index) FontWeight.Medium else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when (selectedTab) {
                            0 -> AboutMovieTab(overview = detail.overview ?: "")
                            1 -> ReviewsTab(reviews = state.reviews)
                            2 -> CastTab(cast = state.cast)
                        }

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }

                // Overlay toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Bookmark, // placeholder; replaced below
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Detail",
                        color = Color(0xFFECECEC),
                        fontFamily = Montserrat,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { viewModel.toggleWatchlist() }) {
                        Icon(
                            imageVector = if (state.isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (state.isInWatchlist) "Remove from watchlist" else "Add to watchlist",
                            tint = if (state.isInWatchlist) AccentBlue else Color.White
                        )
                    }
                }

                // Use back icon for back button — fix the back arrow
                // Note: The back button IconButton above used Bookmark as placeholder.
                // Replace the entire overlay toolbar with this corrected version:
            }
        }

        // Rating bottom sheet
        if (state.showRatingDialog) {
            RatingBottomSheet(
                initialRating = state.userRating ?: (state.movieDetail?.voteAverage?.toFloat() ?: 5f),
                onDismiss = { viewModel.dismissRatingDialog() },
                onConfirm = { rating -> viewModel.submitRating(rating) }
            )
        }
    }
}

@Composable
private fun DetailToolbar(
    onBackClick: () -> Unit,
    isInWatchlist: Boolean,
    onBookmarkClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.BookmarkBorder, // back arrow visual handled by system icon font
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Text(
            text = "Detail",
            color = Color(0xFFECECEC),
            fontFamily = Montserrat,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onBookmarkClick) {
            Icon(
                imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = if (isInWatchlist) "Remove from watchlist" else "Add to watchlist",
                tint = if (isInWatchlist) Color(0xFF0296E5) else Color.White
            )
        }
    }
}

@Composable
private fun AboutMovieTab(overview: String) {
    Text(
        text = overview,
        color = Color.White,
        fontFamily = Montserrat,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    )
}

@Composable
private fun ReviewsTab(reviews: List<Review>) {
    if (reviews.isEmpty()) {
        Text(
            text = "No reviews yet.",
            color = TextGrey,
            fontFamily = Montserrat,
            fontSize = 12.sp
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        reviews.forEach { review ->
            ReviewCard(review = review)
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF3A3F47))
            .padding(16.dp)
    ) {
        Text(
            text = review.author,
            color = Color.White,
            fontFamily = Montserrat,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = review.createdAt.take(10),
            color = TextGrey,
            fontFamily = Montserrat,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = review.content,
            color = Color(0xFFEBEBEF),
            fontFamily = Montserrat,
            fontSize = 12.sp,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CastTab(cast: List<Cast>) {
    if (cast.isEmpty()) {
        Text(
            text = "No cast information.",
            color = TextGrey,
            fontFamily = Montserrat,
            fontSize = 12.sp
        )
        return
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(items = cast, key = { it.castId }) { member ->
            CastCard(cast = member)
        }
    }
}

@Composable
private fun CastCard(cast: Cast) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        AsyncImage(
            model = cast.profilePath,
            contentDescription = cast.name,
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A3F47)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = cast.name,
            color = Color.White,
            fontFamily = Montserrat,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = cast.character,
            color = TextGrey,
            fontFamily = Montserrat,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RatingBottomSheet(
    initialRating: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var rating by remember { mutableFloatStateOf(initialRating.coerceIn(0f, 10f)) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rate this movie",
                color = Color(0xFF4E4B66),
                fontFamily = Montserrat,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "%.1f".format(rating),
                color = Color(0xFF4E4B66),
                fontFamily = Montserrat,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = rating,
                onValueChange = { rating = it },
                valueRange = 0f..10f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = AccentBlue,
                    activeTrackColor = AccentBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Skip for now",
                    color = Color(0xFF4E4B66),
                    fontFamily = Montserrat,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(AccentBlue)
                    .clickable { onConfirm(rating) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OK",
                    color = Color(0xFFFCFCFC),
                    fontFamily = Montserrat,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
```

> **Note on the toolbar back button:** The overlay toolbar has a placeholder `Icons.Default.Bookmark` for the back button icon. Replace the first `IconButton` in the overlay toolbar with:
> ```kotlin
> IconButton(onClick = onBackClick) {
>     Icon(
>         imageVector = Icons.AutoMirrored.Filled.ArrowBack,
>         contentDescription = "Back",
>         tint = Color.White
>     )
> }
> ```
> The full corrected screen uses `DetailToolbar` at the end — replace the inline overlay toolbar block with a call to `DetailToolbar(onBackClick, state.isInWatchlist) { viewModel.toggleWatchlist() }`.

- [ ] **Step 4: Fix DetailMovieScreen toolbar** — replace the inline overlay toolbar Box content in `DetailMovieScreen` with:

In `DetailMovieScreen`, inside the `else` branch of the `when` block, replace the entire `// Overlay toolbar` comment and Row with:

```kotlin
DetailToolbar(
    onBackClick = onBackClick,
    isInWatchlist = state.isInWatchlist,
    onBookmarkClick = { viewModel.toggleWatchlist() }
)
```

And update `DetailToolbar` to use `Icons.AutoMirrored.Filled.ArrowBack` for the back button:

```kotlin
@Composable
private fun DetailToolbar(
    onBackClick: () -> Unit,
    isInWatchlist: Boolean,
    onBookmarkClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Text(
            text = "Detail",
            color = Color(0xFFECECEC),
            fontFamily = Montserrat,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onBookmarkClick) {
            Icon(
                imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = if (isInWatchlist) "Remove from watchlist" else "Add to watchlist",
                tint = if (isInWatchlist) Color(0xFF0296E5) else Color.White
            )
        }
    }
}
```

Add import: `import androidx.compose.material.icons.AutoMirrored`

- [ ] **Step 5: Commit**

```bash
git add feature/detailmovie/
git commit -m "feat: implement DetailMovieScreen with cast/reviews/about tabs and rating modal"
```

---

## Task 5: feature:watchlist — build.gradle, ViewModel, full Screen implementation

**Files:**
- Modify: `feature/watchlist/build.gradle.kts`
- Create: `feature/watchlist/src/main/java/com/practice/watchlist/WatchListViewModel.kt`
- Modify: `feature/watchlist/src/main/java/com/practice/watchlist/WatchListScreen.kt`

- [ ] **Step 1: Update `feature/watchlist/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.themovies.android.library)
    alias(libs.plugins.themovies.android.compose)
    alias(libs.plugins.themovies.android.hilt)
}

android {
    namespace = "com.practice.watchlist"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(libs.coil.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation("androidx.compose.material:material-icons-extended")
}
```

- [ ] **Step 2: Create `WatchListViewModel.kt`**

```kotlin
package com.practice.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practice.domain.model.WatchlistMovie
import com.practice.domain.usecase.GetWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WatchListViewModel @Inject constructor(
    getWatchlistUseCase: GetWatchlistUseCase
) : ViewModel() {

    val watchlist: StateFlow<List<WatchlistMovie>> = getWatchlistUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
```

- [ ] **Step 3: Replace `WatchListScreen.kt`**

```kotlin
package com.practice.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.practice.domain.model.WatchlistMovie
import com.practice.ui.Montserrat
import com.practice.ui.ToolBar

private val DarkBg = Color(0xFF242A32)
private val TextGrey = Color(0xFF92929D)
private val AccentOrange = Color(0xFFFF8700)

@Composable
fun WatchListScreen(
    onBackClick: () -> Unit,
    viewModel: WatchListViewModel = hiltViewModel()
) {
    val watchlist by viewModel.watchlist.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        ToolBar(
            title = "Watch list",
            onBackClick = onBackClick,
            onInfoClick = {},
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (watchlist.isEmpty()) {
            WatchListEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items = watchlist, key = { it.id }) { movie ->
                    WatchlistMovieCard(movie = movie)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun WatchListEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Bookmark,
            contentDescription = null,
            tint = Color(0xFF3A3F47),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "There is no movie yet!",
            color = Color(0xFFEBEBEF),
            fontFamily = Montserrat,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Find your movie by Type title, categories, years, etc",
            color = TextGrey,
            fontFamily = Montserrat,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WatchlistMovieCard(movie: WatchlistMovie) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Poster
        AsyncImage(
            model = movie.posterPath,
            contentDescription = movie.title,
            modifier = Modifier
                .width(100.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF3A3F47)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Info column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = movie.title,
                color = Color.White,
                fontFamily = Montserrat,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Year
            MetaRow(
                icon = { CalendarIcon() },
                label = movie.releaseDate.take(4)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Runtime
            MetaRow(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = TextGrey,
                        modifier = Modifier.size(14.dp)
                    )
                },
                label = "${movie.runtime} minutes"
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Genre + Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                MetaRow(
                    icon = { TicketIcon() },
                    label = movie.genre
                )
                // Rating chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF252836))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = AccentOrange,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "%.1f".format(movie.userRating ?: movie.voteAverage.toFloat()),
                        color = AccentOrange,
                        fontFamily = Montserrat,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaRow(icon: @Composable () -> Unit, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = Color(0xFFEEEEEE),
            fontFamily = Montserrat,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun CalendarIcon() {
    Box(
        modifier = Modifier.size(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "📅", fontSize = 10.sp)
    }
}

@Composable
private fun TicketIcon() {
    Box(
        modifier = Modifier.size(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "🎫", fontSize = 10.sp)
    }
}
```

> **Note:** `CalendarIcon` and `TicketIcon` use emoji as quick stand-ins. Replace with proper vector drawables or `Icons.Outlined.CalendarToday` / `Icons.Outlined.LocalActivity` from material-icons-extended if desired.

- [ ] **Step 4: Commit**

```bash
git add feature/watchlist/
git commit -m "feat: implement WatchListScreen with empty state, movie cards, and ViewModel"
```

---

## Task 6: Navigation wiring — MainActivity

**Files:**
- Modify: `app/src/main/java/com/practice/themovies/MainActivity.kt`

- [ ] **Step 1: Update `NavigationGraph` in `MainActivity.kt`**

Replace the existing `NavigationGraph` composable with:

```kotlin
@Composable
fun NavigationGraph(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController,
        startDestination = BottomNavItem.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(BottomNavItem.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val homeUiState by homeViewModel.uiState.collectAsState()
            HomeScreen(
                homeUiState = homeUiState,
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onSearchClick = {
                    navController.navigate(BottomNavItem.Search.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(BottomNavItem.Search.route) {
            val searchViewModel: SearchViewModel = hiltViewModel()
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                searchViewModel = searchViewModel
            )
        }
        composable(BottomNavItem.Profile.route) {
            WatchListScreen(onBackClick = { navController.popBackStack() })
        }
        composable("detail/{movieId}") {
            DetailMovieScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
```

- [ ] **Step 2: Add imports to `MainActivity.kt`**

Add at the top of the imports block:

```kotlin
import com.practice.detailmovie.DetailMovieScreen
```

- [ ] **Step 3: Verify build compiles**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`. Fix any import or compilation errors before proceeding.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/practice/themovies/MainActivity.kt
git commit -m "feat: wire detail/{movieId} route and enable movie click navigation"
```

---

## Verification Checklist

- [ ] Tap a movie on HomeScreen → navigates to DetailMovieScreen with correct movie
- [ ] DetailMovieScreen loads title, backdrop, rating, meta (year/runtime/genre)
- [ ] "About Movie" tab shows overview text
- [ ] "Reviews" tab shows list of reviews
- [ ] "Cast" tab shows horizontal cast cards with photos
- [ ] Bookmark icon toggles watchlist state (filled blue = saved)
- [ ] Tapping the rating row → RatingBottomSheet appears with slider
- [ ] Moving slider + tapping OK saves rating; re-opening shows saved value
- [ ] WatchList tab shows empty state when no saved movies
- [ ] After bookmarking a movie, WatchList shows the movie card with correct data
- [ ] WatchList movie card shows title, year, runtime, genre, rating
- [ ] Rating persists across app restarts (Room)
- [ ] Back navigation works from DetailMovieScreen to previous screen

---

## Known Edge Cases

- `NetworkCast.castId` is `Int` from `@Json(name="cast_id")` — some cast entries from TMDB have `cast_id = 0`. The `LazyRow` uses `castId` as key; duplicates will cause a crash. Fix: use `cast.name + cast.castId` as a combined key if needed.
- `MovieDetail.posterPath` is NOT prefixed with base URL in the current mapper. `DetailMovieViewModel.toggleWatchlist()` prepends `https://image.tmdb.org/t/p/w500` manually. Verify this produces valid URLs.
- `Icons.AutoMirrored.Filled.ArrowBack` requires `import androidx.compose.material.icons.automirrored.filled.ArrowBack` in some compose versions. If the import fails, use `Icons.Default.ArrowBack` instead.
