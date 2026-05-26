package com.example.soccerworld.ui.chatbot

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soccerworld.R
import com.example.soccerworld.ui.theme.AccentEmerald
import com.example.soccerworld.ui.theme.BrandNavy
import com.example.soccerworld.ui.theme.SofascoreBlue

@Composable
fun ChatFab(
    isScrollingDown: Boolean,
    onClick: () -> Unit
) {
    // Buttery-smooth scale and alpha transitions driven by scroll direction
    val scale by animateFloatAsState(targetValue = if (isScrollingDown) 0f else 1f)
    val alpha by animateFloatAsState(targetValue = if (isScrollingDown) 0f else 1f)

    Row(
        modifier = Modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = alpha
            )
            .padding(end = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // Speech Bubble Tooltip
        AnimatedVisibility(
            visible = !isScrollingDown,
            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End) + slideInHorizontally(initialOffsetX = { it / 2 }),
            exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End) + slideOutHorizontally(targetOffsetX = { it / 2 })
        ) {
            Surface(
                color = AccentEmerald,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 4.dp
                ),
                modifier = Modifier.padding(end = 12.dp),
                shadowElevation = 6.dp
            ) {
                Text(
                    text = "Hỏi tôi về trận đấu hôm nay nhé! 🤖",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandNavy,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        // Vibrant Floating Action Button with Navy-Emerald Gradient
        FloatingActionButton(
            onClick = onClick,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 12.dp
            ),
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(AccentEmerald, SofascoreBlue)))
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = stringResource(id = R.string.chatbot_content_description_fab),
                modifier = Modifier.size(24.dp),
                tint = BrandNavy
            )
        }
    }
}
