package com.practice.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.practice.domain.model.Movie
import com.practice.ui.Montserrat

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    onMovieClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
) {
    val tabs = listOf("Now playing", "Upcoming", "Top rated", "Popular")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val movies = when (selectedTabIndex) {
        0 -> homeUiState.nowPlayingMovies
        1 -> homeUiState.upcomingMovies
        2 -> homeUiState.topRatedMovies
        3 -> homeUiState.popularMovies
        else -> emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        TopBar()
        Spacer(modifier = Modifier.height(8.dp))
        SearchBar(onSearchClick = onSearchClick)
        Spacer(modifier = Modifier.height(16.dp))
        FeaturedMoviesSection(
            featuredMovies = homeUiState.topRatedMovies,
            onMovieClick = onMovieClick
        )
        CategoryTabsSection(tabs = tabs) {
            selectedTabIndex = tabs.indexOf(it)
        }
        Spacer(modifier = Modifier.height(16.dp))
        MovieGridSection(movies, onMovieClick)
    }
}

@Composable
fun TopBar(modifier: Modifier = Modifier) {
    Text(
        text = "What do you want to watch?",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            fontSize = 18.sp
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    placeholderText: String = "Search",
    onSearchClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0xFF2C2C2C), shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp)
            .clickable {
                onSearchClick()
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = placeholderText,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                painter = painterResource(R.drawable.ic_search_left),
                contentDescription = "Search Icon",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun MoviePosterWithNumber(movie: Movie, index: Int, onMovieClick: (Movie) -> Unit) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .width(160.dp)
            .height(260.dp)
            .clickable {
                onMovieClick(movie)
            }
    ) {
        Image(
            painter = rememberAsyncImagePainter(movie.posterPath),
            contentDescription = "Movie Poster",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, bottom = 42.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray)
        )

        StrokedText(
            text = "${index + 1}",
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            fontSize = 96.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
        )
    }
}

@Composable
fun StrokedText(
    text: String,
    fontSize: TextUnit = 96.sp,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.SemiBold,
    strokeColor: Color = Color(0xFF0296E5),
    fillColor: Color = Color(0xFF242A32)
) {
    Box(
        modifier = modifier
            .wrapContentSize()
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            color = strokeColor,
            style = TextStyle(
                drawStyle = Stroke(
                    width = 5f
                )
            )
        )
        Text(
            text = text,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            color = fillColor
        )
    }
}

@Composable
fun FeaturedMoviesSection(featuredMovies: List<Movie>, onMovieClick: (Int) -> Unit) {
    if (featuredMovies.isEmpty()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            userScrollEnabled = false
        ) {
            items(5) {
                MoviePosterWithNumber(
                    movie = Movie(
                        id = 0,
                        title = "",
                        overview = "",
                        posterPath = "",
                        backdropPath = "",
                        releaseDate = "",
                        voteAverage = 0.0,
                        voteCount = 0
                    ),
                    index = it
                ) {}
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            itemsIndexed(
                items = featuredMovies,
                key = { _, movie ->
                    movie.id
                }
            ) { index, movie ->
                MoviePosterWithNumber(movie = movie, index = index) {
                    onMovieClick(movie.id)
                }
            }
        }
    }
}

@Composable
fun CategoryTabsSection(
    modifier: Modifier = Modifier,
    tabs: List<String>,
    onTabSelected: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    ScrollableTabRow(
        modifier = modifier,
        containerColor = Color.Transparent,
        selectedTabIndex = selectedTab,
        edgePadding = 0.dp,
        indicator = { tabPositions ->
            Box(
                Modifier
                    .tabIndicatorOffset(tabPositions[selectedTab])
                    .height(3.dp)
                    .padding(horizontal = 16.dp)
                    .background(Color(0xFF3A3F47))
            )
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = {
                    selectedTab = index
                    onTabSelected(title)
                },
                text = {
                    Text(
                        text = title,
                        color = if (selectedTab == index) Color.White else Color.Gray
                    )
                }
            )
        }
    }
}

@Composable
fun MovieGridSection(movies: List<Movie>, onMovieClick: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = movies, key = { it.id }) { movie ->
            MovieCard(movie = movie, onMovieClick = onMovieClick)
        }
    }
}

@Composable
fun MovieCard(movie: Movie, onMovieClick: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Gray)
            .clickable {
                onMovieClick(movie.id)
            }
    ) {
        Image(
            painter = rememberAsyncImagePainter(movie.posterPath),
            contentDescription = "Movie Poster",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}


@Preview
@Composable
fun HomeScreenPreview() {
    val movies = listOf(
        Movie(
            id = 1,
            title = "Movie 1",
            overview = "Overview 1",
            posterPath = "https://example.com/poster1.jpg",
            backdropPath = "https://example.com/backdrop1.jpg",
            releaseDate = "2023-01-01",
            voteAverage = 8.0,
            voteCount = 100
        ),
        Movie(
            id = 2,
            title = "Movie 2",
            overview = "Overview 2",
            posterPath = "https://example.com/poster2.jpg",
            backdropPath = "https://example.com/backdrop2.jpg",
            releaseDate = "2023-02-01",
            voteAverage = 7.5,
            voteCount = 200
        )
    )

    HomeScreen(
        homeUiState = HomeUiState(topRatedMovies = movies, nowPlayingMovies = movies),
        onMovieClick = {},
        onSearchClick = {})
}
