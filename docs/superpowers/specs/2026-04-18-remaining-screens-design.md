# Remaining Screens Design

**Date:** 2026-04-18  
**Scope:** Detail Movie, WatchList, Rate Movie modal — full implementation with Room persistence

---

## Summary

Three screens remain: **Detail Movie** (backdrop + tabs for About/Reviews/Cast + rating modal), **WatchList** (empty + populated states), and **Rate Movie** (bottom sheet dialog overlaying Detail). Navigation wiring completes the app.

---

## Layer Changes

### core:domain

**MovieRepository** — add two methods:
```
getReviews(movieId: Int): List<Review>
getCast(movieId: Int): List<Cast>
```

**LocalRepository** — implement:
```
saveToWatchlist(movie: Movie, runtime: Int, genre: String)
removeFromWatchlist(movieId: Int)
getWatchlist(): Flow<List<WatchlistMovie>>
isInWatchlist(movieId: Int): Flow<Boolean>
saveRating(movieId: Int, rating: Float)
getRating(movieId: Int): Flow<Float?>
```

**New domain model:** `WatchlistMovie` — mirrors `Movie` but adds `runtime`, `genre`, `userRating?`

**New use cases:**
- `GetMovieCastUseCase`
- `GetMovieReviewsUseCase`
- `SaveToWatchlistUseCase`
- `RemoveFromWatchlistUseCase`
- `GetWatchlistUseCase`
- `IsInWatchlistUseCase`
- `SaveRatingUseCase`

---

### core:database

**WatchlistEntity** (`@Entity(tableName = "watchlist")`):
- `movieId: Int` (primary key)
- `title: String`
- `posterPath: String?`
- `backdropPath: String?`
- `releaseDate: String`
- `voteAverage: Double`
- `runtime: Int`
- `genre: String`
- `userRating: Float?`

**WatchlistDao:**
- `@Insert(onConflict = REPLACE) suspend fun insert(entity)`
- `@Delete suspend fun delete(entity)` / `@Query DELETE by id`
- `@Query("SELECT * FROM watchlist") fun getAll(): Flow<List<WatchlistEntity>>`
- `@Query("SELECT EXISTS(...) FROM watchlist WHERE movieId=:id") fun exists(id): Flow<Boolean>`
- `@Query("UPDATE watchlist SET userRating=:rating WHERE movieId=:id") suspend fun updateRating(id, rating)`

**TheMoviesDatabase:** `@Database(entities = [WatchlistEntity::class], version = 1)`

**DatabaseModule** (Hilt `@Module`): provides `TheMoviesDatabase` and `WatchlistDao` as singletons.

---

### core:data

**MovieRepositoryImpl** — add `getReviews` and `getCast` implementations mapping network → domain.

**LocalRepositoryImpl** — new class injecting `WatchlistDao`, implements `LocalRepository`.

**DataModule** — bind `LocalRepositoryImpl` to `LocalRepository`.

---

### feature:detailmovie

**DetailUiState:**
```kotlin
data class DetailUiState(
    val movieDetail: MovieDetail? = null,
    val cast: List<Cast> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val isInWatchlist: Boolean = false,
    val userRating: Float? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
```

**DetailMovieViewModel:**
- Loads `movieDetail`, `cast`, `reviews` concurrently via `supervisorScope { async }` on init
- Observes `isInWatchlist(movieId)` and `getRating(movieId)` as Flows combined into state
- `toggleWatchlist()` — save or remove
- `submitRating(rating: Float)` — saves rating, requires movie already in watchlist

**DetailMovieScreen layout:**
```
Box {
  AsyncImage(backdropPath, fillMaxWidth, height=280dp)
  Box(darkGradientOverlay, fillMaxWidth, height=280dp)
  TopAppBar(transparent bg, back arrow, "Detail", bookmark icon)
  
  Column(verticalScroll) {
    Spacer(200dp)  // clears image area
    // Info card overlapping image
    Column(bg=#242a32, cornerRadius=topStart=16/topEnd=16) {
      Text(title, Poppins 18sp bold, white)
      RatingRow(star, voteAverage orange)
      MetaRow(year | runtime min | genre, grey icons)
      TabRow(About Movie / Reviews / Cast, indicator=#0296e5)
      // tab content
    }
  }
}
```

**About Movie tab:** `Text(overview)` scrollable  
**Reviews tab:** `LazyColumn` of `ReviewCard(author, createdAt, content)`  
**Cast tab:** `LazyRow` of `CastCard(profileImage, name, character)`

**Rate Movie bottom sheet** (triggered by bookmark icon if already in watchlist, or separate rate button):
- `ModalBottomSheet` with `Slider(0f..10f, steps=19)`
- Shows current value as `"%.1f".format(value)` in 32sp
- "Skip for now" → dismiss, "OK" → `submitRating(value)` → dismiss

---

### feature:watchlist

**WatchListViewModel:**
- Collects `GetWatchlistUseCase()` as `StateFlow<List<WatchlistMovie>>`

**WatchListScreen:**
- **Empty state:** centered column with folder illustration (vector drawable) + "There is no movie yet!" (Montserrat 16sp SemiBold, #ebebef) + subtitle (Montserrat 12sp, #92929d)
- **Populated state:** `LazyColumn` of `WatchlistMovieCard`

**WatchlistMovieCard** (matches Figma):
- Row layout: poster image (100dp wide, 16dp radius) | Column(title, year row, runtime row, genre row, rating chip)
- Meta rows: icon (calendar/clock/ticket) + text, #eeeeee, Poppins 12sp
- Rating chip: star icon + orange score (Montserrat 12sp SemiBold, #ff8700), bg rounded

---

### Navigation (app module)

Add to `NavHost`:
```kotlin
composable("detail/{movieId}") { backStack ->
    val movieId = backStack.arguments?.getString("movieId")?.toInt() ?: return@composable
    val vm: DetailMovieViewModel = hiltViewModel()
    DetailMovieScreen(movieId = movieId, onBackClick = { navController.popBackStack() })
}
```

Uncomment navigation call in HomeScreen: `navController.navigate("detail/$movieId")`

---

## Error Handling

- Detail screen: if any of the 3 concurrent calls fail, show partial data (supervisor scope isolates failures). If `movieDetail` itself fails, show error state.
- WatchList: Room Flow never errors; empty list → empty state.
- Rate dialog: only available after movie is saved to watchlist (bookmark icon saves first, then allows rating).

---

## Out of Scope

- Splash screen (trivial, not in bottom nav)
- Reviews/Cast pagination (single-page load matches existing API pattern)
- Offline caching of remote data beyond watchlist
