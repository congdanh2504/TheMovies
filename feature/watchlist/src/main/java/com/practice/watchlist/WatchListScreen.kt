package com.practice.watchlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.practice.ui.ToolBar

@Composable
fun WatchListScreen(
    onBackClick : () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ToolBar(
            title = "Watch list",
            onBackClick = onBackClick,
            onInfoClick = { /* TODO */ },
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}