package com.practice.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ToolBar(
    modifier: Modifier = Modifier,
    title: String,
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit,
    infoIcon: Int? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_icon),
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .clickable(onClick = onBackClick)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontFamily = Montserrat,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
        if (infoIcon != null) {
            Icon(
                painter = painterResource(infoIcon),
                contentDescription = "Info",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .clickable(onClick = onInfoClick)
            )
        } else {
            Spacer(modifier = Modifier.size(36.dp))
        }
    }

}

// preview
@Preview()
@Composable
fun ToolBarPreview() {
    ToolBar(
        title = "Movie Title",
        onBackClick = {},
        onInfoClick = {},
        infoIcon = 0
    )
}

