# Navigation 3 Migration Design

**Date:** 2026-04-21  
**Scope:** Migrate from `androidx.navigation:navigation-compose` 2.9.0 to `androidx.navigation3:navigation3-ui` using typed destinations and a ViewModel-owned back stack.  
**Goal:** Single-pane phone layout; learn Nav3's distinctive primitives (typed destinations, `NavDisplay`, back stack as state).

---

## Motivation

- Replace stringly-typed routes with compile-time-safe destination objects.
- Adopt Nav3's "back stack as plain observable list" model — explicit, readable, testable.
- Keep feature modules unchanged (callback-driven, navigation-agnostic).

---

## Architecture

### Approach: Simple ViewModel Holder (Approach A)

`NavigationViewModel` (in `app` module) owns the back stack as `mutableStateListOf<Any>`. `MainActivity` reads it and passes navigation lambdas into feature composables. Feature modules have no awareness of Nav3 — their signatures stay identical.

---

## Typed Destinations

**File:** `app/src/main/java/com/practice/themovies/navigation/Destinations.kt`

```kotlin
@Serializable object HomeDestination
@Serializable object SearchDestination
@Serializable object WatchlistDestination
@Serializable data class DetailDestination(val movieId: Int)
```

`DetailDestination` carries `movieId` as a property — no URL templates or string parsing. `BottomNavItem` references destinations by type instead of route string:

```kotlin
sealed class BottomNavItem(val destination: Any, val icon: Int, val label: String) {
    object Home     : BottomNavItem(HomeDestination,     R.drawable.ic_home,   "Home")
    object Search   : BottomNavItem(SearchDestination,   R.drawable.ic_search, "Search")
    object Watchlist: BottomNavItem(WatchlistDestination,R.drawable.ic_save,   "Watch List")
}
```

---

## NavigationViewModel

**File:** `app/src/main/java/com/practice/themovies/navigation/NavigationViewModel.kt`

```kotlin
@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {

    val backStack = mutableStateListOf<Any>(HomeDestination)

    fun navigate(destination: Any)  { backStack.add(destination) }
    fun navigateToTab(destination: Any) { backStack.clear(); backStack.add(destination) }
    fun popBack() { if (backStack.size > 1) backStack.removeLast() }
}
```

| Method | Use case |
|---|---|
| `navigate` | Push Detail (or any non-tab destination) |
| `navigateToTab` | Bottom nav tap — clear stack, push tab root |
| `popBack` | System back / back arrow in Detail |

`navigateToTab` replaces the old `popUpTo + launchSingleTop + restoreState` flags with an explicit list mutation.

---

## NavDisplay in MainActivity

`NavHost` → `NavDisplay`. The entry provider block maps each typed destination to its composable. Bottom bar visibility is derived from the type of the last back-stack entry — no string comparison.

```kotlin
val navViewModel: NavigationViewModel = hiltViewModel()
val backStack = navViewModel.backStack
val currentDestination = backStack.lastOrNull()
val showBottomBar = currentDestination !is DetailDestination

Scaffold(
    bottomBar = {
        if (showBottomBar) BottomNavigationBar(
            currentDestination = currentDestination,
            onTabSelected = { navViewModel.navigateToTab(it) }
        )
    }
) { padding ->
    NavDisplay(
        backStack = backStack,
        onBack = { navViewModel.popBack() },
        modifier = Modifier.padding(padding),
        entryProvider = entryProvider {
            entry<HomeDestination> {
                val vm: HomeViewModel = hiltViewModel()
                HomeScreen(
                    homeUiState = vm.uiState.collectAsState().value,
                    onMovieClick = { navViewModel.navigate(DetailDestination(it)) },
                    onSearchClick = { navViewModel.navigate(SearchDestination) }
                )
            }
            entry<SearchDestination> {
                val vm: SearchViewModel = hiltViewModel()
                SearchScreen(
                    onMovieClick = { navViewModel.navigate(DetailDestination(it)) },
                    onBackClick  = { navViewModel.popBack() }
                )
            }
            entry<WatchlistDestination> {
                val vm: WatchListViewModel = hiltViewModel()
                WatchListScreen(
                    onMovieClick = { navViewModel.navigate(DetailDestination(it)) }
                )
            }
            entry<DetailDestination> { dest ->
                DetailMovieScreen(
                    movieId    = dest.movieId,
                    onBackClick = { navViewModel.popBack() }
                )
            }
        }
    )
}
```

### DetailMovieViewModel change

`movieId` can no longer be extracted from `SavedStateHandle` via a route argument. Two options:
- Pass `movieId` directly to `DetailMovieScreen` and into the ViewModel via a custom `ViewModelProvider.Factory` or `@AssistedInject`.
- Keep `SavedStateHandle` but populate it manually — this is more complex and not idiomatic in Nav3.

**Decision:** use Hilt's `@AssistedInject` for `DetailMovieViewModel` so `movieId` is passed at creation time.

---

## Dependencies

### `gradle/libs.versions.toml`

```toml
[versions]
navigation3             = "1.0.0-alpha01"
kotlinxSerializationJson = "1.7.3"
# remove: navigationCompose = "2.9.0"

[libraries]
androidx-navigation3-ui     = { module = "androidx.navigation3:navigation3-ui", version.ref = "navigation3" }
kotlinx-serialization-json  = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }
# remove: androidx-navigation-compose

[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

### `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.kotlin.serialization)   // add
}
dependencies {
    implementation(libs.androidx.navigation3.ui)        // add
    implementation(libs.kotlinx.serialization.json)     // add
    implementation(libs.androidx.hilt.navigation.compose) // keep
    // remove: implementation(libs.androidx.navigation.compose)
}
```

---

## What Changes vs What Stays

| Area | Current | After migration |
|---|---|---|
| Routes | String constants (`"home"`, `"detail/{movieId}"`) | Typed objects (`HomeDestination`, `DetailDestination(id)`) |
| Navigation controller | `NavController` via `rememberNavController()` | `NavigationViewModel.backStack` |
| Nav host | `NavHost { composable(...) }` | `NavDisplay { entryProvider { entry<T> } }` |
| Back handling | Implicit (NavController) | `onBack` lambda in `NavDisplay` |
| Bottom bar visibility | String route comparison | `is DetailDestination` type check |
| Bottom nav state | `popUpTo + launchSingleTop + restoreState` | `backStack.clear(); backStack.add(dest)` |
| Feature modules | Unchanged — callback-driven | Unchanged |
| DetailMovieViewModel | `SavedStateHandle["movieId"]` | `@AssistedInject` with `movieId` param |

---

## Files Created / Modified

| Action | File |
|---|---|
| Create | `app/.../navigation/Destinations.kt` |
| Create | `app/.../navigation/NavigationViewModel.kt` |
| Modify | `app/.../MainActivity.kt` |
| Modify | `feature/detailmovie/.../DetailMovieViewModel.kt` |
| Modify | `gradle/libs.versions.toml` |
| Modify | `app/build.gradle.kts` |
| Modify | `build-logic/...` (serialization plugin declaration) |
