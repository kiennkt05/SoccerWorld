package com.example.soccerworld.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.example.soccerworld.R

@Composable
fun TeamCrestImage(
    model: Any?,
    size: Dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val ballPainter = painterResource(id = R.drawable.ic_ball)
    
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit,
        placeholder = ballPainter,
        error = ballPainter,
        fallback = ballPainter
    )
}
