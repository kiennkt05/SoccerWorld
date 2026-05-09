package com.example.soccerworld.ui.fixture.detail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.soccerworld.data.remote.flashlive.dto.Formation
import com.example.soccerworld.data.remote.flashlive.dto.LineupPlayer
import com.example.soccerworld.ui.theme.SoccerWorldTheme
import com.example.soccerworld.util.buildFormationLayers
import com.example.soccerworld.R

@Composable
fun InteractivePitchView(homeFormation: Formation, awayFormation: Formation) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f / 2.2f) // 1. Fixed Aspect Ratio to 1:2.2
            .clip(RectangleShape)
            .background(Color(0xFF1E5A22))
    ) {
        val pitchWidth = this.maxWidth
        val pitchHeight = this.maxHeight

        SoccerPitchBackground()

        val homeLayers = buildFormationLayers(homeFormation, isHome = true)
        val numHomeLayers = homeLayers.size
        homeLayers.forEachIndexed { layerIdx, layerPlayers ->
            val yFraction = if (numHomeLayers > 1) {
                0.05f + (layerIdx.toFloat() / (numHomeLayers - 1)) * 0.40f
            } else {
                0.25f
            }

            val numPlayersInLayer = layerPlayers.size
            layerPlayers.forEachIndexed { playerIdx, player ->
                val xFraction = (playerIdx + 0.5f) / numPlayersInLayer

                val markerWidth = 60.dp
                val markerHeight = 64.dp
                val xDp = (pitchWidth * xFraction) - (markerWidth / 2)
                val yDp = (pitchHeight * yFraction) - (markerHeight / 2)

                Box(
                    modifier = Modifier
                        .offset(x = xDp, y = yDp)
                        .width(markerWidth)
                        .height(markerHeight)
                ) {
                    PitchPlayerMarker(player = player, isHome = true)
                }
            }
        }

        val awayLayers = buildFormationLayers(awayFormation, isHome = false)
        val numAwayLayers = awayLayers.size
        awayLayers.forEachIndexed { layerIdx, layerPlayers ->
            val yFraction = if (numAwayLayers > 1) {
                0.56f + (layerIdx.toFloat() / (numAwayLayers - 1)) * 0.4f
            } else {
                0.75f
            }

            val numPlayersInLayer = layerPlayers.size
            layerPlayers.forEachIndexed { playerIdx, player ->
                val xFraction = (playerIdx + 0.5f) / numPlayersInLayer

                val markerWidth = 60.dp
                val markerHeight = 64.dp
                val xDp = (pitchWidth * xFraction) - (markerWidth / 2)
                val yDp = (pitchHeight * yFraction) - (markerHeight / 2)

                Box(
                    modifier = Modifier
                        .offset(x = xDp, y = yDp)
                        .width(markerWidth)
                        .height(markerHeight)
                ) {
                    PitchPlayerMarker(player = player, isHome = false)
                }
            }
        }
    }
}

@Composable
fun SoccerPitchBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(color = Color(0xFF3C7F62))

        val strokeWidth = 1.5.dp.toPx()
        val lineColor = Color.White.copy(alpha = 0.2f)

        drawRect(color = lineColor, style = Stroke(width = strokeWidth))

        drawLine(
            color = lineColor,
            start = Offset(0f, h / 2f),
            end = Offset(w, h / 2f),
            strokeWidth = strokeWidth
        )

        drawCircle(
            color = lineColor,
            radius = w * 0.15f,
            center = Offset(w / 2f, h / 2f),
            style = Stroke(width = strokeWidth)
        )

        drawCircle(
            color = lineColor,
            radius = 1.5.dp.toPx(),
            center = Offset(w / 2f, h / 2f)
        )

        drawRect(
            color = lineColor,
            topLeft = Offset(w * 0.26f, 0f),
            size = Size(w * 0.48f, h * 0.07f),
            style = Stroke(width = strokeWidth)
        )
        drawRect(
            color = lineColor,
            topLeft = Offset(w * 0.38f, 0f),
            size = Size(w * 0.24f, h * 0.03f),
            style = Stroke(width = strokeWidth)
        )

        drawRect(
            color = lineColor,
            topLeft = Offset(w * 0.26f, h - (h * 0.07f)),
            size = Size(w * 0.48f, h * 0.07f),
            style = Stroke(width = strokeWidth)
        )
        drawRect(
            color = lineColor,
            topLeft = Offset(w * 0.38f, h - (h * 0.03f)),
            size = Size(w * 0.24f, h * 0.03f),
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun PitchPlayerMarker(player: LineupPlayer, isHome: Boolean, modifier: Modifier = Modifier) {
    val isSubbedOut = player.incidents?.contains(6) == true
    val alpha = 1f

    val imageUrl = if (player.imageId != null) {
        "https://www.flashscore.com/res/image/data/${player.imageId}"
    } else null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center
        ) {
            // Anchor element: Avatar Circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = if (isHome) Color(0xFF1E88E5) else Color(0xFFE53935),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = player.shortName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        alpha = alpha
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.sports_soccer),
                        contentDescription = null,
                        tint = if (isHome) Color(0xFF1E88E5).copy(alpha = alpha) else Color(0xFFE53935).copy(alpha = alpha),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 2. Event Overlays Distributed to corners
            val hasYellowCard = player.incidents?.contains(1) == true
            val hasRedCard = player.incidents?.contains(2) == true
            val hasGoal = player.incidents?.any { it == 3 || it == 10 } == true
            val hasAssist = player.incidents?.contains(8) == true // Assumed code 8 for Assist

            if (hasRedCard) {
                Box(modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-2).dp, y = (-2).dp)) { IncidentIcon(2) }
            } else if (hasYellowCard) {
                Box(modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-2).dp, y = (-2).dp)) { IncidentIcon(1) }
            }

            if (hasGoal) {
                Box(modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)) { IncidentIcon(3) }
            }

            if (isSubbedOut) {
                Box(modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-2).dp, y = 2.dp)) { IncidentIcon(6) }
            }

            if (hasAssist) {
                Box(modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)) { IncidentIcon(8) }
            }

            // 4. Square Rating Badge with minimal padding
            if (player.rating != null) {
                val ratingColor = getRatingColor(player.rating)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 6.dp)
                        .size(17.dp) // 1. Force a strict width and height to guarantee a square
                        .background(ratingColor, shape = RectangleShape)
                        .border(0.5.dp, Color.White.copy(alpha = 0.5f), RectangleShape),
                    // 2. Padding is completely removed so the fixed size dictates the shape
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.rating,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1, // Prevents any wrapping issues with '10'
                        style = TextStyle(
                            // 1. Strip the default Android font padding
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            ),
                            // 2. Force the line height to strictly hug the glyphs
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            )
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        // 3. Player Name string formatted as requested
        val captainTag = if (player.fullName.contains("(C)", ignoreCase = true)) "(C) " else ""
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.LightGray)) {
                    append("${player.number} ")
                }
                append(captainTag)
                append(player.shortName)
            },
            style = TextStyle(
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.8f),
                    offset = Offset(0.5f, 0.5f),
                    blurRadius = 1f
                )
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.5.dp)
        )
    }
}

@Composable
fun IncidentIcon(code: Int) {
    val iconSize = 14.dp
    val modifier = Modifier
        .size(iconSize)
        .background(Color.White, CircleShape)
        .padding(0.5.dp)

    when (code) {
        1 -> Icon(painter = painterResource(id = R.drawable.rectangle), contentDescription = "Yellow Card", tint = Color(0xFFFBC02D), modifier = modifier.scale(scaleX = 0.6f, scaleY = 1f))
        2 -> Icon(painter = painterResource(id = R.drawable.rectangle), contentDescription = "Red Card", tint = Color(0xFFD32F2F), modifier = modifier.scale(scaleX = 0.6f, scaleY = 1f))
        3, 10 -> Icon(painter = painterResource(id = R.drawable.ic_ball), contentDescription = "Goal", tint = Color.Black, modifier = modifier)
        6 -> Icon(painter = painterResource(id = R.drawable.swap_horiz), contentDescription = "Subbed Out", tint = Color(0xFFD32F2F), modifier = modifier)
        7 -> Icon(painter = painterResource(id = R.drawable.swap_horiz), contentDescription = "Subbed In", tint = Color(0xFF388E3C), modifier = modifier)
        8 -> Icon(painter = painterResource(id = R.drawable.shoe_cleats), contentDescription = "Assist", modifier = modifier.graphicsLayer(rotationY = 180f)) // Assumed 8 for assist
    }
}

@Preview(showBackground = true)
@Composable
fun InteractivePitchViewPreview() {
    val mockHomePlayers = listOf(
        LineupPlayer("1", "Alisson Becker", "(C) Henderson", 1, "7.5", null, null, 1),
        LineupPlayer("2", "T. Alexander-Arnold", "A. Arnold", 66, "8.0", null, null, 2),
        LineupPlayer("3", "Virgil van Dijk", "van Dijk", 4, "7.2", null, null, 2),
        LineupPlayer("4", "Ibrahima Konate", "Konate", 5, "6.8", null, null, 2),
        LineupPlayer("5", "Andrew Robertson", "Robertson", 26, "7.1", null, null, 2),
        LineupPlayer("6", "Alexis Mac Allister", "Mac Allister", 10, "7.4", null, null, 3),
        LineupPlayer("7", "Dominik Szoboszlai", "Szoboszlai", 8, "7.0", null, listOf(1), 3),
        LineupPlayer("8", "Curtis Jones", "Jones", 17, "6.9", null, null, 3),
        LineupPlayer("9", "Mohamed Salah", "Salah", 11, "8.5", null, listOf(3,8), 4),
        LineupPlayer("10", "Darwin Nunez", "Nunez", 9, "6.5", null, listOf(6), 4),
        LineupPlayer("11", "Luis Diaz", "Diaz", 7, "7.3", null, null, 4)
    )

    val mockAwayPlayers = listOf(
        LineupPlayer("12", "Ederson Moraes", "Ederson", 31, "7.2", null, null, 1),
        LineupPlayer("13", "Kyle Walker", "Walker", 2, "7.0", null, null, 2),
        LineupPlayer("14", "Ruben Dias", "Dias", 3, "7.4", null, null, 2),
        LineupPlayer("15", "Manuel Akanji", "Akanji", 25, "6.9", null, null, 2),
        LineupPlayer("16", "Nathan Ake", "Ake", 6, "7.1", null, null, 2),
        LineupPlayer("17", "Rodri", "Rodri", 16, "8.2", null, null, 3),
        LineupPlayer("18", "Kevin De Bruyne", "De Bruyne", 17, "8.8", null, listOf(10), 3),
        LineupPlayer("19", "Bernardo Silva", "Silva", 20, "7.6", null, null, 3),
        LineupPlayer("20", "Phil Foden", "Foden", 47, "8.1", null, null, 4),
        LineupPlayer("21", "Erling Haaland", "Haaland", 9, "7.9", null, listOf(3), 4),
        LineupPlayer("22", "Jeremy Doku", "Doku", 11, "7.5", null, listOf(7), 4)
    )

    val mockHomeFormation = Formation(1, "1-4-3-3", mockHomePlayers)
    val mockAwayFormation = Formation(2, "1-4-3-3", mockAwayPlayers)

    SoccerWorldTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            InteractivePitchView(
                homeFormation = mockHomeFormation,
                awayFormation = mockAwayFormation
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SoccerPitchBackgroundPreview() {
    SoccerWorldTheme {
        SoccerPitchBackground(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PitchPlayerMarkerPreview() {
    val samplePlayer = LineupPlayer(
        id = "1",
        fullName = "Mohamed Salah (C)",
        shortName = "Salah",
        number = 11,
        rating = "8.5",
        imageId = null,
        incidents = listOf(3, 8),
        position = 4
    )
    SoccerWorldTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFF1B4D22))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            PitchPlayerMarker(player = samplePlayer, isHome = true)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IncidentIconPreview() {
    SoccerWorldTheme {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IncidentIcon(code = 1) // Yellow Card
            IncidentIcon(code = 2) // Red Card
            IncidentIcon(code = 3) // Goal
            IncidentIcon(code = 6) // Subbed Out
            IncidentIcon(code = 7) // Subbed In
            IncidentIcon(code = 8) // Assist
        }
    }
}
