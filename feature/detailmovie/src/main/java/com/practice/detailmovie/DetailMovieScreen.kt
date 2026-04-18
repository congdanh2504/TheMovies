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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

private val TAB_LABELS = listOf("About Movie", "Reviews", "Cast")

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
                    // ── Backdrop + poster + rating badge ──────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(271.dp)
                    ) {
                        // Backdrop
                        AsyncImage(
                            model = detail.backdropPath,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0x44000000), DarkBg)
                                    )
                                )
                        )
                        // Rating badge — top-right over backdrop
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 148.dp, end = 16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x52252836))
                                .clickable { viewModel.showRatingDialog() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
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
                        // Poster thumbnail — bottom-start, overlaps backdrop
                        AsyncImage(
                            model = detail.posterPath?.let {
                                "https://image.tmdb.org/t/p/w500$it"
                            },
                            contentDescription = detail.title,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 29.dp)
                                .size(width = 95.dp, height = 120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF3A3F47)),
                            contentScale = ContentScale.Crop
                        )
                        // Title — to the right of poster, anchored to bottom
                        Text(
                            text = detail.title,
                            color = Color.White,
                            fontFamily = Montserrat,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 136.dp, end = 29.dp, bottom = 4.dp)
                        )
                    }

                    // ── Meta row ──────────────────────────────────────────────
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.padding(start = 29.dp),
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

                    // ── Tabs ──────────────────────────────────────────────────
                    Spacer(modifier = Modifier.height(24.dp))
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
                        TAB_LABELS.forEachIndexed { index, title ->
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

                    // ── Tab content ───────────────────────────────────────────
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        when (selectedTab) {
                            0 -> AboutMovieTab(overview = detail.overview ?: "")
                            1 -> ReviewsTab(reviews = state.reviews)
                            2 -> CastTab(cast = state.cast)
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }

                DetailToolbar(
                    onBackClick = onBackClick,
                    isInWatchlist = state.isInWatchlist,
                    onBookmarkClick = { viewModel.toggleWatchlist() }
                )
            }
        }

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
                contentDescription = null,
                tint = if (isInWatchlist) AccentBlue else Color.White
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
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        reviews.forEach { review ->
            ReviewCard(review = review)
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar + rating column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(44.dp)
        ) {
            AsyncImage(
                model = review.avatarPath,
                contentDescription = review.author,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A3F47)),
                contentScale = ContentScale.Crop
            )
            review.rating?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "%.1f".format(it),
                    color = AccentBlue,
                    fontFamily = Montserrat,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
        // Content column
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = review.author,
                color = Color.White,
                fontFamily = Montserrat,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.content,
                color = Color.White,
                fontFamily = Montserrat,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        }
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
    val rows = cast.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { member ->
                    CastCard(
                        cast = member,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CastCard(cast: Cast, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        AsyncImage(
            model = cast.profilePath,
            contentDescription = cast.name,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A3F47)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = cast.name,
            color = Color.White,
            fontFamily = Montserrat,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
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
