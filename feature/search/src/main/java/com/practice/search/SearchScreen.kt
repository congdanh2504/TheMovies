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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.compose.runtime.Composable
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
    val query by searchViewModel.query.collectAsState()
    val searchResults = searchViewModel.searchResults.collectAsLazyPagingItems()

    Column(modifier = Modifier.fillMaxSize()) {
        ToolBar(
            title = "Search",
            onBackClick = onBackClick,
            onInfoClick = { /* TODO */ },
            infoIcon = R.drawable.ic_search_info,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        SearchBar(
            modifier = Modifier.padding(horizontal = 16.dp),
            onTextChanged = { searchViewModel.onQueryChanged(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        val isRefreshing = searchResults.loadState.refresh is LoadState.Loading
        val refreshError = searchResults.loadState.refresh as? LoadState.Error

        when {
            query.isBlank() -> NoResultView(modifier = Modifier.padding(horizontal = 94.dp))

            isRefreshing && searchResults.itemCount == 0 -> Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            refreshError != null -> Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = refreshError.error.message ?: "Something went wrong",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(onClick = { searchResults.retry() }) {
                        Text("Retry")
                    }
                }
            }

            searchResults.itemCount == 0 -> NoResultView(modifier = Modifier.padding(horizontal = 94.dp))

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(
                    count = searchResults.itemCount,
                    key = searchResults.itemKey { it.id }
                ) { index ->
                    val movie = searchResults[index]
                    if (movie != null) {
                        SearchMovie(
                            movie = movie,
                            posterPainter = rememberAsyncImagePainter(movie.posterPath),
                            onClick = onMovieClick
                        )
                    }
                }

                item {
                    when (val appendState = searchResults.loadState.append) {
                        is LoadState.Loading -> Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                        is LoadState.Error -> Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = appendState.error.message ?: "Load failed",
                                color = Color.Red
                            )
                            Button(onClick = { searchResults.retry() }) {
                                Text("Retry")
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}

@Composable
fun NoResultView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(R.drawable.ic_no_results),
            contentDescription = "No Result",
            modifier = Modifier.size(76.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "We are sorry, we can not find the movie :(",
            color = Color.White,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
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
