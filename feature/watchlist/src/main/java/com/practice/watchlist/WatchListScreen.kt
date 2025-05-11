package com.practice.watchlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun WatchListScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("WatchList", color = Color.White) }
}