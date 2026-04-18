package com.practice.search.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.practice.domain.model.Movie

@Composable
fun SearchMovie(
    movie: Movie,
    modifier: Modifier = Modifier,
    posterPainter: Painter,
    onClick: (Int) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(movie.id) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = posterPainter,
            contentDescription = "Movie Poster",
            modifier = Modifier
                .width(100.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 22.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            RatingRow(rating = movie.voteAverage)
            Spacer(modifier = Modifier.height(4.dp))
            ReleaseYearRow(releaseYear = movie.releaseDate.take(4).ifEmpty { "N/A" })
        }
    }
}

@Composable
fun RatingRow(rating: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = com.practice.search.R.drawable.ic_star),
            contentDescription = "Rating Star",
            tint = Color(0xFFFF8700),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = String.format("%.1f", rating),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFFFF8700),
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
fun CategoryRow(category: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = com.practice.search.R.drawable.ic_category_bookmark),
            contentDescription = "Category Icon",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Gray
            )
        )
    }
}

@Composable
fun ReleaseYearRow(releaseYear: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(com.practice.search.R.drawable.ic_calendar),
            contentDescription = "Release Year Icon",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = releaseYear,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Gray
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MovieDetailHeaderPreview() {
    val sampleMovie = Movie(
        id = 1,
        title = "Spiderman: Far From Home", // Tiêu đề dài hơn để kiểm tra maxLines
        overview = "Peter Parker and his friends go on a summer trip to Europe.",
        posterPath = "https://image.tmdb.org/t/p/w500/rjbJd1A488xV4KjNn8wPzN1XjJ.jpg", // Ví dụ poster URL
        backdropPath = "https://example.com/backdrop.jpg",
        releaseDate = "2019-07-02",
        voteAverage = 9.5,
        voteCount = 12345
    )
    MaterialTheme { // Wrap trong MaterialTheme để có typography và màu sắc
        SearchMovie(
            movie = sampleMovie,
            posterPainter = rememberAsyncImagePainter(sampleMovie.posterPath), // Thay bằng URL poster thật hoặc resource ID
        )
    }
}