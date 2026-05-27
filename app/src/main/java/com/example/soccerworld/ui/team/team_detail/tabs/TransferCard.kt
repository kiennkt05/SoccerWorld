package com.example.soccerworld.ui.team.team_detail.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.soccerworld.ui.theme.BrandGreenMedium
import com.example.soccerworld.ui.theme.TextDark
import com.example.soccerworld.ui.theme.TextSecondary
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.soccerworld.R
import com.example.soccerworld.data.remote.flashlive.TransferData
import com.example.soccerworld.data.remote.flashlive.TransferPlayer
import com.example.soccerworld.data.remote.flashlive.TransferTeam
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransferCard(
    transfer: TransferData,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp), // Slightly sharper corners matching the web UI
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // Very subtle shadow
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ─── Left Column: Player Info ───────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transfer.player?.value ?: "Unknown",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark // Crisp dark text
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val flagPainter = playerAvatarPainter(transfer.player?.image)
                    if (flagPainter != null) {
                        // Use Image instead of Icon so the flag retains its original colors
                        Image(
                            painter = flagPainter,
                            contentDescription = "Country Flag",
                            modifier = Modifier
                                .width(18.dp)
                                .height(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = transfer.player?.countryName ?: "Unknown",
                        fontSize = 14.sp,
                        color = TextSecondary // Muted gray
                    )
                }
            }

            // ─── Right Column: Transfer Details ─────────────────────────────────
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BadgeImage(url = transfer.fromTeam?.image)

                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "To",
                        tint = Color(0xFF999999),
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(16.dp)
                    )

                    BadgeImage(url = transfer.toTeam?.image)

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatDate(transfer.transferDate),
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = transfer.transferTypeStr ?: "Transfer",
                    fontSize = 14.sp,
                    color = BrandGreenMedium // Transfer type accent
                )
            }
        }
    }
}

@Composable
private fun BadgeImage(url: String?) {
    if (url.isNullOrBlank()) {
        Icon(
            painter = painterResource(id = R.drawable.ic_ball),
            contentDescription = null,
            modifier = Modifier.size(20.dp), // Scaled down to match the capture
            tint = Color.LightGray
        )
        return
    }
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = Modifier.size(20.dp), // Scaled down to match the capture
        placeholder = painterResource(id = R.drawable.ic_ball),
        error = painterResource(id = R.drawable.ic_ball),
        fallback = painterResource(id = R.drawable.ic_ball)
    )
}

private fun formatDate(timestamp: Long?): String {
    if (timestamp == null) return ""
    val date = Date(timestamp * 1000)
    // Removed leading zero for single-digit days to match "2 Feb 2026"
    val formatter = SimpleDateFormat("d MMM yyyy", Locale.US)
    return formatter.format(date)
}

@Composable
private fun playerAvatarPainter(imageName: String?): Painter? {
    if (imageName.isNullOrBlank()) return null
    val resName = imageName.replace("flag-", "country_flag_").replace("-", "_")
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
    return if (resId != 0) painterResource(id = resId) else null
}

@Preview(showBackground = true)
@Composable
fun TransferCardPreview() {
    val sampleTransfer = TransferData(
        transferDate = 1770000000L,
        transferTypeStr = "Transfer Free",
        transferDirection = "out",
        fromTeam = TransferTeam(image = "https://www.flashscore.com/res/image/data/fojwJwZA-2Vnc5VQf.png", value = "Newcastle"),
        toTeam = TransferTeam(image = "https://www.flashscore.com/res/image/data/Em1CYqYg-UNZg5BP0.png", value = "Leicester"),
        player = TransferPlayer(participantId = "123", value = "Jamaal Lascelles", image = "flag-198", countryName = "England")
    )
    TransferCard(transfer = sampleTransfer)
}