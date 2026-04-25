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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
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
        modifier = Modifier.semantics { testTagsAsResourceId = true },
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
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<HomeDestination> {
                    val homeViewModel: HomeViewModel = hiltViewModel()
                    val homeUiState by homeViewModel.uiState.collectAsState()
                    val selectedTabIndex by homeViewModel.selectedTabIndex.collectAsState()
                    HomeScreen(
                        homeUiState = homeUiState,
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = homeViewModel::onTabSelected,
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
                    modifier = Modifier.testTag("nav_${item.label.lowercase().replace(" ", "_")}"),
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
