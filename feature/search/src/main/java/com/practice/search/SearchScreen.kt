package com.practice.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.practice.search.widget.SearchMovie
import com.practice.ui.SearchBar
import com.practice.ui.ToolBar

@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit = {},
    searchViewModel: SearchViewModel,
) {
    val state by searchViewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ToolBar(
            title = "Search",
            onBackClick = onBackClick,
            onInfoClick = { /* TODO */ },
            infoIcon = R.drawable.ic_search_info,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        SearchBar(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            onTextChanged = { q ->
                searchViewModel.processIntent(SearchIntent.QueryChanged(q))
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (!state.isLoading && state.movies.isEmpty()) {
            NoResultView(
                modifier = Modifier
                    .padding(horizontal = 94.dp)
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(items = state.movies, key = { it.id }) {
                SearchMovie(
                    movie = it,
                    posterPainter = rememberAsyncImagePainter(it.posterPath),
                    onClick = onMovieClick
                )
            }

            item {
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }

                    }
                    state.error != null -> {
                        Text(
                            text = "Error: ${state.error}",
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    else -> {
                        LaunchedEffect(state.movies.size) {
                            if (state.movies.isNotEmpty()) {
                                searchViewModel.processIntent(SearchIntent.LoadMore)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoResultView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(R.drawable.ic_no_results),
            contentDescription = "No Result",
            modifier = Modifier.size(76.dp)
        )
        Spacer(
            modifier = modifier
                .height(16.dp)
        )
        Text(
            text = "We are sorry, we can not find the movie :(",
            color = Color.White,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Spacer(
            modifier = modifier
                .height(8.dp)
        )
        Text(
            text = "Find your movie by Type title, categories, years, etc ",
            color = Color.Gray,
            style = TextStyle(
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}
