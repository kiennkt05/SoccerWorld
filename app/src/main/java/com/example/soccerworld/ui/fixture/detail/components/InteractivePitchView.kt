package com.example.soccerworld.ui.fixture.detail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.soccerworld.data.remote.flashlive.dto.Formation
import com.example.soccerworld.data.remote.flashlive.dto.LineupPlayer
import com.example.soccerworld.util.buildFormationLayers

@Composable
fun InteractivePitchView(homeFormation: Formation, awayFormation: Formation) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.65f) // Optimized for mobile screens
            .background(Color(0xFF2E7D32)) // Sofascore green
    ) {
        // 1. Draw the pitch lines
        SoccerPitchBackground()

        // 2. Map the players over the pitch
        Column(modifier = Modifier.fillMaxSize()) {
            
            // --- HOME TEAM HALF (Top) ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                val homeLayers = buildFormationLayers(homeFormation, isHome = true)
                
                homeLayers.forEach { layerPlayers ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        layerPlayers.forEach { player ->
                            PlayerPitchNode(player = player)
                        }
                    }
                }
            }

            // --- AWAY TEAM HALF (Bottom) ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                val awayLayers = buildFormationLayers(awayFormation, isHome = false)
                
                awayLayers.forEach { layerPlayers ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        layerPlayers.forEach { player ->
                            PlayerPitchNode(player = player)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoccerPitchBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 1.5.dp.toPx()
        val lineColor = Color.White.copy(alpha = 0.5f)
        val w = size.width
        val h = size.height

        // Outer bounds
        drawRect(color = lineColor, style = Stroke(width = strokeWidth))

        // Center line
        drawLine(
            color = lineColor,
            start = Offset(0f, h / 2f),
            end = Offset(w, h / 2f),
            strokeWidth = strokeWidth
        )

        // Center circle
        drawCircle(
            color = lineColor,
            radius = w * 0.18f,
            center = Offset(w / 2f, h / 2f),
            style = Stroke(width = strokeWidth)
        )

        // Top Penalty Box
        drawRect(
            color = lineColor,
            topLeft = Offset(w * 0.25f, 0f),
            size = Size(w * 0.5f, h * 0.14f),
            style = Stroke(width = strokeWidth)
        )

        // Bottom Penalty Box
        drawRect(
            color = lineColor,
            topLeft = Offset(w * 0.25f, h - (h * 0.14f)),
            size = Size(w * 0.5f, h * 0.14f),
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun PlayerPitchNode(player: LineupPlayer) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp) // Fixed width prevents long names from breaking the Row layout
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            
            val isSubbedOut = player.incidents?.contains(6) == true
            val alpha = if (isSubbedOut) 0.5f else 1f

            val imageUrl = if (player.imageId != null) {
                "https://www.flashscore.com/res/image/data/${player.imageId}"
            } else null

            AsyncImage(
                model = imageUrl,
                contentDescription = player.shortName,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f)),
                alpha = alpha
            )

            // Incident Icons (Top Right offset)
            if (!player.incidents.isNullOrEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-4).dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    player.incidents.take(2).forEach { code -> // Take max 2 so it doesn't clutter
                        IncidentIcon(code = code) 
                    }
                }
            }

            // Rating Badge (Bottom overlap)
            if (player.rating != null) {
                val ratingDouble = player.rating.toDoubleOrNull() ?: 0.0
                Box(
                    modifier = Modifier
                        .offset(y = 6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(getRatingColor(ratingDouble))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = player.rating,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Jersey Number and Player Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (player.number != null) {
                Text(
                    text = player.number.toString(),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            val captainTag = if (player.fullName.contains("(C)")) "(C) " else ""
            Text(
                text = captainTag + player.shortName,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}

@Composable
fun IncidentIcon(code: Int) {
    val iconSize = 12.dp
    val modifier = Modifier.size(iconSize).background(Color.White, CircleShape).padding(1.dp)
    
    when (code) {
        1 -> Icon(Icons.Default.Warning, contentDescription = "Yellow Card", tint = Color(0xFFFFC107), modifier = modifier)
        2 -> Icon(Icons.Default.Warning, contentDescription = "Red Card", tint = Color(0xFFE53935), modifier = modifier)
        3, 10 -> Icon(Icons.Default.SportsSoccer, contentDescription = "Goal", tint = Color.Black, modifier = modifier)
        6 -> Icon(Icons.Default.SwapHoriz, contentDescription = "Subbed Out", tint = Color.Red, modifier = modifier)
        7 -> Icon(Icons.Default.SwapHoriz, contentDescription = "Subbed In", tint = Color.Green, modifier = modifier)
    }
}

fun getRatingColor(rating: Double): Color {
    return when {
        rating >= 8.0 -> Color(0xFF1E88E5) // Blue 
        rating >= 7.0 -> Color(0xFF43A047) // Green 
        rating >= 6.5 -> Color(0xFFFFB300) // Orange 
        else -> Color(0xFFE53935)          // Red 
    }
}
