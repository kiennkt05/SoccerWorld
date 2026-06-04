package com.example.soccerworld.ui.fixture.detail.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soccerworld.model.matchdetail.MatchLineupTeam


@Composable
internal fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚽", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

internal fun MatchLineupTeam.toFormation(isHome: Boolean): com.example.soccerworld.data.remote.flashlive.dto.Formation {
    var disp = this.formation?.takeIf { it.isNotBlank() } ?: "4-4-2"
    val sum = disp.split("-").mapNotNull { it.toIntOrNull() }.sum()
    if (sum < 11 && !disp.startsWith("1-")) {
        disp = "1-$disp"
    }

    val members = this.starters.mapIndexed { index, p ->
        val safeName = p.name
        com.example.soccerworld.data.remote.flashlive.dto.LineupPlayer(
            id = index.toString(),
            fullName = safeName + if (p.isCaptain) " (C)" else "",
            shortName = p.shortName.takeIf { it.isNotBlank() } ?: safeName.split(" ").lastOrNull() ?: safeName,
            number = p.number,
            rating = p.rating,
            imageId = p.imageUrl,
            incidents = p.incidents,
            position = p.fieldPosition ?: (index + 1)
        )
    }
    return com.example.soccerworld.data.remote.flashlive.dto.Formation(
        teamSide = if (isHome) 1 else 2,
        disposition = disp,
        members = members
    )
}
