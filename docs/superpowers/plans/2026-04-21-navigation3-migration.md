# Navigation 3 Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace `androidx.navigation:navigation-compose` 2.9.0 with `androidx.navigation3:navigation3-ui`, typed destinations, and a `NavigationViewModel`-owned back stack.

**Architecture:** `NavigationViewModel` holds `mutableStateListOf<Any>` as the back stack; `MainActivity` reads it and wires `NavDisplay` with an `entryProvider` block; feature modules are unchanged. `DetailMovieViewModel` switches from `SavedStateHandle` to `@AssistedInject` to receive `movieId` directly.

**Tech Stack:** Navigation 3 (`navigation3-ui:1.0.0-alpha01`), Kotlin Serialization (`kotlinx-serialization-json:1.7.3`), Hilt `@AssistedInject`, Jetpack Compose, Hilt 2.56.2.

---

## File Map

| Action | Path | Responsibility |
|---|---|---|
| Modify | `gradle/libs.versions.toml` | Add nav3 + serialization versions/libs; remove nav2 |
| Modify | `app/build.gradle.kts` | Swap nav2 → nav3 dep; add serialization plugin |
| Create | `app/src/main/java/com/practice/themovies/navigation/Destinations.kt` | Typed destination objects + updated `BottomNavItem` |
| Create | `app/src/main/java/com/practice/themovies/navigation/NavigationViewModel.kt` | Back stack ViewModel |
| Create | `app/src/test/java/com/practice/themovies/navigation/NavigationViewModelTest.kt` | Unit tests for back stack logic |
| Modify | `feature/detailmovie/src/main/java/com/practice/detailmovie/DetailMovieViewModel.kt` | `@AssistedInject`; remove `SavedStateHandle` |
| Modify | `feature/detailmovie/src/main/java/com/practice/detailmovie/DetailMovieScreen.kt` | Add `movieId: Int` param; use `hiltViewModel` factory |
| Modify | `app/src/main/java/com/practice/themovies/MainActivity.kt` | Replace `NavHost`/`NavController` with `NavDisplay`/`NavigationViewModel` |

---

## Task 1: Update Gradle Dependencies

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add nav3 and serialization to version catalog**

Open `gradle/libs.versions.toml`. In `[versions]`, remove `navigationCompose = "2.9.0"` and add:

```toml
navigation3 = "1.0.0-alpha01"
kotlinxSerializationJson = "1.7.3"
```

In `[libraries]`, remove:
```toml
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigationCompose" }
```

Add:
```toml
androidx-navigation3-ui = { module = "androidx.navigation3:navigation3-ui", version.ref = "navigation3" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }
```

The `kotlin-serialization` plugin entry already exists in `[plugins]` — no change needed there.

- [ ] **Step 2: Update app/build.gradle.kts**

Open `app/build.gradle.kts`. Add the serialization plugin and swap the nav dependency:

```kotlin
plugins {
    alias(libs.plugins.themovies.android.application)
    alias(libs.plugins.themovies.android.compose)
    alias(libs.plugins.themovies.android.hilt)
    alias(libs.plugins.kotlin.serialization)   // ADD
}
```

In `dependencies`, replace `implementation(libs.androidx.navigation.compose)` with:
```kotlin
implementation(libs.androidx.navigation3.ui)
implementation(libs.kotlinx.serialization.json)
```

Keep all other dependencies unchanged. Final dependencies block:
```kotlin
dependencies {
    implementation(project(":feature:home"))
    implementation(project(":feature:detailmovie"))
    implementation(project(":feature:search"))
    implementation(project(":feature:watchlist"))
    implementation(project(":core:data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.androidx.hilt.navigation.compose)
}
```

- [ ] **Step 3: Sync Gradle and verify no red errors**

Run:
```bash
./gradlew :app:dependencies | grep navigation
```

Expected: `navigation3-ui` appears; `navigation-compose` does NOT appear.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "build: replace navigation-compose 2.9.0 with navigation3-ui 1.0.0-alpha01"
```

---

## Task 2: Create Typed Destinations

**Files:**
- Create: `app/src/main/java/com/practice/themovies/navigation/Destinations.kt`

- [ ] **Step 1: Create Destinations.kt**

```kotlin
package com.practice.themovies.navigation

import com.practice.themovies.R
import kotlinx.serialization.Serializable

@Serializable object HomeDestination
@Serializable object SearchDestination
@Serializable object WatchlistDestination
@Serializable data class DetailDestination(val movieId: Int)

sealed class BottomNavItem(val destination: Any, val icon: Int, val label: String) {
    object Home     : BottomNavItem(HomeDestination,     R.drawable.ic_home,   "Home")
    object Search   : BottomNavItem(SearchDestination,   R.drawable.ic_search, "Search")
    object Watchlist: BottomNavItem(WatchlistDestination, R.drawable.ic_save,  "Watch List")
}
```

- [ ] **Step 2: Verify it compiles**

```bash
./gradlew :app:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL (or only errors from MainActivity which still references old types — that's fine for now).

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/practice/themovies/navigation/Destinations.kt
git commit -m "feat: add typed Nav3 destinations and updated BottomNavItem"
```

---

## Task 3: Create NavigationViewModel + Unit Tests

**Files:**
- Create: `app/src/main/java/com/practice/themovies/navigation/NavigationViewModel.kt`
- Create: `app/src/test/java/com/practice/themovies/navigation/NavigationViewModelTest.kt`

- [ ] **Step 1: Write the failing tests first**

Create `app/src/test/java/com/practice/themovies/navigation/NavigationViewModelTest.kt`:

```kotlin
package com.practice.themovies.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationViewModelTest {

    private fun viewModel() = NavigationViewModel()

    @Test
    fun `initial back stack contains HomeDestination`() {
        val vm = viewModel()
        assertEquals(listOf(HomeDestination), vm.backStack.toList())
    }

    @Test
    fun `navigate pushes destination onto back stack`() {
        val vm = viewModel()
        vm.navigate(DetailDestination(42))
        assertEquals(listOf(HomeDestination, DetailDestination(42)), vm.backStack.toList())
    }

    @Test
    fun `popBack removes last destination`() {
        val vm = viewModel()
        vm.navigate(SearchDestination)
        vm.popBack()
        assertEquals(listOf(HomeDestination), vm.backStack.toList())
    }

    @Test
    fun `popBack does not remove last item when stack has one entry`() {
        val vm = viewModel()
        vm.popBack()
        assertEquals(listOf(HomeDestination), vm.backStack.toList())
    }

    @Test
    fun `navigateToTab clears stack and pushes tab destination`() {
        val vm = viewModel()
        vm.navigate(DetailDestination(1))
        vm.navigateToTab(SearchDestination)
        assertEquals(listOf(SearchDestination), vm.backStack.toList())
    }

    @Test
    fun `navigateToTab from nested stack replaces entire stack`() {
        val vm = viewModel()
        vm.navigate(SearchDestination)
        vm.navigate(DetailDestination(5))
        vm.navigateToTab(WatchlistDestination)
        assertEquals(listOf(WatchlistDestination), vm.backStack.toList())
    }
}
```

- [ ] **Step 2: Run tests — expect FAIL (class not found)**

```bash
./gradlew :app:test --tests "com.practice.themovies.navigation.NavigationViewModelTest"
```

Expected: BUILD FAILS — `NavigationViewModel` does not exist yet.

- [ ] **Step 3: Create NavigationViewModel.kt**

```kotlin
package com.practice.themovies.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {

    val backStack = mutableStateListOf<Any>(HomeDestination)

    fun navigate(destination: Any) {
        backStack.add(destination)
    }

    fun navigateToTab(destination: Any) {
        backStack.clear()
        backStack.add(destination)
    }

    fun popBack() {
        if (backStack.size > 1) backStack.removeLast()
    }
}
```

- [ ] **Step 4: Run tests — expect PASS**

```bash
./gradlew :app:test --tests "com.practice.themovies.navigation.NavigationViewModelTest"
```

Expected: 6 tests pass. If `mutableStateListOf` fails in JVM context (no Compose runtime), replace the backStack field with:
```kotlin
private val _backStack = mutableListOf<Any>(HomeDestination)
val backStack: MutableList<Any> get() = _backStack
```
…then re-run until green.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/practice/themovies/navigation/NavigationViewModel.kt \
        app/src/test/java/com/practice/themovies/navigation/NavigationViewModelTest.kt
git commit -m "feat: add NavigationViewModel with mutableStateListOf back stack"
```

---

## Task 4: Update DetailMovieViewModel to @AssistedInject

**Files:**
- Modify: `feature/detailmovie/src/main/java/com/practice/detailmovie/DetailMovieViewModel.kt`

- [ ] **Step 1: Replace @HiltViewModel + SavedStateHandle with @AssistedInject**

Replace the entire file content:

```kotlin
package com.practice.detailmovie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practice.domain.model.Cast
import com.practice.domain.model.MovieDetail
import com.practice.domain.model.Review
import com.practice.domain.model.WatchlistMovie
import com.practice.domain.usecase.GetMovieCastUseCase
import com.practice.domain.usecase.GetMovieDetailsUseCase
import com.practice.domain.usecase.GetMovieReviewsUseCase
import com.practice.domain.usecase.GetRatingUseCase
import com.practice.domain.usecase.IsInWatchlistUseCase
import com.practice.domain.usecase.RemoveFromWatchlistUseCase
import com.practice.domain.usecase.SaveRatingUseCase
import com.practice.domain.usecase.SaveToWatchlistUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

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

@HiltViewModel(assistedFactory = DetailMovieViewModel.Factory::class)
class DetailMovieViewModel @AssistedInject constructor(
    @Assisted val movieId: Int,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
    private val getMovieCastUseCase: GetMovieCastUseCase,
    private val getMovieReviewsUseCase: GetMovieReviewsUseCase,
    private val isInWatchlistUseCase: IsInWatchlistUseCase,
    private val saveToWatchlistUseCase: SaveToWatchlistUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
    private val saveRatingUseCase: SaveRatingUseCase,
    private val getRatingUseCase: GetRatingUseCase
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(movieId: Int): DetailMovieViewModel
    }

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        loadData()
        observeWatchlistStatus()
        observeRating()
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

    private fun observeRating() {
        viewModelScope.launch {
            getRatingUseCase(movieId).collect { rating ->
                _state.update { it.copy(userRating = rating) }
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
                saveToWatchlistUseCase(detail.toWatchlistMovie())
            }
        }
    }

    fun showRatingDialog() = _state.update { it.copy(showRatingDialog = true) }

    fun dismissRatingDialog() = _state.update { it.copy(showRatingDialog = false) }

    fun submitRating(rating: Float) {
        viewModelScope.launch {
            val current = _state.value
            val detail = current.movieDetail ?: return@launch
            if (!current.isInWatchlist) {
                saveToWatchlistUseCase(detail.toWatchlistMovie())
            }
            saveRatingUseCase(movieId, rating)
            _state.update { it.copy(showRatingDialog = false) }
        }
    }

    private fun MovieDetail.toWatchlistMovie() = WatchlistMovie(
        id = id,
        title = title,
        posterPath = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        runtime = runtime?.toInt() ?: 0,
        genre = genres.firstOrNull()?.name ?: ""
    )
}
```

- [ ] **Step 2: Verify feature:detailmovie compiles**

```bash
./gradlew :feature:detailmovie:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add feature/detailmovie/src/main/java/com/practice/detailmovie/DetailMovieViewModel.kt
git commit -m "refactor: migrate DetailMovieViewModel to @AssistedInject for Nav3 compatibility"
```

---

## Task 5: Update DetailMovieScreen to Accept movieId

**Files:**
- Modify: `feature/detailmovie/src/main/java/com/practice/detailmovie/DetailMovieScreen.kt`

- [ ] **Step 1: Add movieId param and update hiltViewModel call**

Find the `DetailMovieScreen` function signature at line 75. Change it from:

```kotlin
@Composable
fun DetailMovieScreen(
    onBackClick: () -> Unit,
    viewModel: DetailMovieViewModel = hiltViewModel()
)
```

To:

```kotlin
@Composable
fun DetailMovieScreen(
    movieId: Int,
    onBackClick: () -> Unit,
    viewModel: DetailMovieViewModel = hiltViewModel<DetailMovieViewModel, DetailMovieViewModel.Factory>(
        creationCallback = { factory -> factory.create(movieId) }
    )
)
```

No other changes to the screen body — it still reads `viewModel.state` the same way.

- [ ] **Step 2: Update imports — add the overloaded hiltViewModel import**

Make sure the import for `hiltViewModel` stays as-is:
```kotlin
import androidx.hilt.navigation.compose.hiltViewModel
```

The `hiltViewModel<VM, F>(creationCallback)` overload is provided by `hilt-navigation-compose:1.2.0` — no additional dependency needed.

- [ ] **Step 3: Verify feature:detailmovie compiles**

```bash
./gradlew :feature:detailmovie:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add feature/detailmovie/src/main/java/com/practice/detailmovie/DetailMovieScreen.kt
git commit -m "feat: add movieId param to DetailMovieScreen for Nav3 typed destinations"
```

---

## Task 6: Rewrite MainActivity with NavDisplay

**Files:**
- Modify: `app/src/main/java/com/practice/themovies/MainActivity.kt`

- [ ] **Step 1: Replace the entire MainActivity.kt content**

```kotlin
package com.practice.themovies

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.entryProvider
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.practice.detailmovie.DetailMovieScreen
import com.practice.home.HomeScreen
import com.practice.home.HomeViewModel
import com.practice.search.SearchScreen
import com.practice.search.SearchViewModel
import com.practice.themovies.navigation.BottomNavItem
import com.practice.themovies.navigation.DetailDestination
import com.practice.themovies.navigation.HomeDestination
import com.practice.themovies.navigation.NavigationViewModel
import com.practice.themovies.navigation.SearchDestination
import com.practice.themovies.navigation.WatchlistDestination
import com.practice.themovies.ui.theme.DarkGray
import com.practice.themovies.ui.theme.TheMoviesTheme
import com.practice.watchlist.WatchListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            TheMoviesTheme {
                val systemUiController = rememberSystemUiController()
                val backgroundColor = DarkGray
                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = backgroundColor,
                        darkIcons = false
                    )
                }
                MainScaffold()
            }
        }
    }
}

@Composable
fun MainScaffold() {
    val navViewModel: NavigationViewModel = hiltViewModel()
    val backStack = navViewModel.backStack
    val currentDestination = backStack.lastOrNull()
    val showBottomBar = currentDestination !is DetailDestination

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentDestination = currentDestination,
                    onTabSelected = { navViewModel.navigateToTab(it) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { navViewModel.popBack() },
            modifier = Modifier.padding(innerPadding),
            entryProvider = entryProvider {
                entry<HomeDestination> {
                    val homeViewModel: HomeViewModel = hiltViewModel()
                    val homeUiState by homeViewModel.uiState.collectAsState()
                    HomeScreen(
                        homeUiState = homeUiState,
                        onMovieClick = { movieId ->
                            navViewModel.navigate(DetailDestination(movieId))
                        },
                        onSearchClick = {
                            navViewModel.navigate(SearchDestination)
                        }
                    )
                }
                entry<SearchDestination> {
                    val searchViewModel: SearchViewModel = hiltViewModel()
                    SearchScreen(
                        onBackClick = { navViewModel.popBack() },
                        onMovieClick = { movieId ->
                            navViewModel.navigate(DetailDestination(movieId))
                        },
                        searchViewModel = searchViewModel
                    )
                }
                entry<WatchlistDestination> {
                    WatchListScreen(
                        onBackClick = { navViewModel.popBack() },
                        onMovieClick = { movieId ->
                            navViewModel.navigate(DetailDestination(movieId))
                        }
                    )
                }
                entry<DetailDestination> { dest ->
                    DetailMovieScreen(
                        movieId = dest.movieId,
                        onBackClick = { navViewModel.popBack() }
                    )
                }
            }
        )
    }
}

@Composable
fun BottomNavigationBar(
    currentDestination: Any?,
    onTabSelected: (Any) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Watchlist
    )
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            items.forEach { item ->
                val selected = currentDestination?.let { it::class == item.destination::class } ?: false
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.label
                        )
                    },
                    label = { Text(item.label) },
                    selected = selected,
                    onClick = { if (!selected) onTabSelected(item.destination) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.secondary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = Color(0x00FFFFFF)
                    )
                )
            }
        }
    }
}
```

- [ ] **Step 2: Full debug build**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL. If the import `androidx.navigation3.ui.NavDisplay` or `entryProvider` is wrong due to alpha API changes, check the actual package by looking at the navigation3-ui artifact sources or the error message and adjust the import.

- [ ] **Step 3: Run unit tests**

```bash
./gradlew test
```

Expected: All tests pass, including the 6 `NavigationViewModelTest` tests.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/practice/themovies/MainActivity.kt
git commit -m "feat: migrate to Navigation 3 — NavDisplay, typed destinations, NavigationViewModel back stack"
```

---

## Task 7: Verify End-to-End

- [ ] **Step 1: Install on device/emulator**

```bash
./gradlew installDebug
```

Expected: APK installs without error.

- [ ] **Step 2: Manual smoke test**

1. App opens → Home tab shown, bottom nav visible
2. Tap Search tab → Search screen, bottom nav visible, Home deselected
3. Tap a movie → Detail screen, bottom nav hidden
4. Press back → returns to Search, bottom nav visible
5. Tap Watchlist tab → Watchlist screen
6. Tap a movie → Detail screen
7. Tap back → Watchlist screen
8. Tap Home tab from Watchlist → Home screen (stack cleared, not stacked)

- [ ] **Step 3: Final commit if any fixups were needed**

```bash
git add -p
git commit -m "fix: Nav3 migration fixups after manual testing"
```

---

## Import Reference

If the Nav3 alpha changes package names, the correct imports as of `1.0.0-alpha01` are:

```kotlin
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.entryProvider
import androidx.navigation3.runtime.entry   // if entryProvider DSL moves to runtime module
```

Check actual package by running:
```bash
./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep navigation3
```
Then inspect the AAR or the error output from a failed compile for the real package paths.
