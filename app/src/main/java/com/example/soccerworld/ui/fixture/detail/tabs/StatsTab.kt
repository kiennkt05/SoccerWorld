package com.example.soccerworld.ui.fixture.detail.tabs

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.soccerworld.R
import com.example.soccerworld.data.remote.flashlive.EventStatsGroup
import com.example.soccerworld.data.remote.flashlive.EventStatsItem
import com.example.soccerworld.data.remote.flashlive.EventStatsStage
import com.example.soccerworld.model.h2h.AwayTeamX
import com.example.soccerworld.model.h2h.FullTime
import com.example.soccerworld.model.h2h.HomeTeamX
import com.example.soccerworld.model.h2h.Matche
import com.example.soccerworld.model.h2h.Score
import com.example.soccerworld.model.matchdetail.MatchDetailAggregate
import com.example.soccerworld.model.matchdetail.MatchEnrichmentDetail
import com.example.soccerworld.model.matchdetail.MatchEvent
import com.example.soccerworld.model.matchdetail.MatchHighlight
import com.example.soccerworld.model.matchdetail.MatchLineupPlayer
import com.example.soccerworld.model.matchdetail.MatchLineupTeam
import com.example.soccerworld.model.matchdetail.MatchNews
import com.example.soccerworld.model.statistic.AwayTeam
import com.example.soccerworld.model.statistic.HomeTeam
import com.example.soccerworld.model.statistic.StatisticsResponse
import com.example.soccerworld.ui.fixture.detail.components.HighlightList
import com.example.soccerworld.ui.fixture.detail.components.getRatingColor
import com.example.soccerworld.ui.theme.SoccerWorldTheme
import com.example.soccerworld.ui.theme.LocalSoccerColors

import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import com.example.soccerworld.model.statistic.Score as StatScore
import com.example.soccerworld.model.statistic.FullTime as StatFullTime

// ── Stats Tab ────────────────────────────────────────────────────────────────

@Composable
fun StatsTab(stages: List<EventStatsStage>) {
    if (stages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không có thống kê", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Top Stage Toggle (ALL / 1ST / 2ND)
        StageToggleBar(
            stages = stages,
            selectedIndex = selectedTabIndex,
            onSelect = { selectedTabIndex = it }
        )

        val selectedStage = stages.getOrNull(selectedTabIndex)

        // 2. Scrollable List of Stat Groups (Flattened)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            selectedStage?.groups?.forEach { group ->
                item(key = "group_${group.groupLabel ?: group.hashCode()}", contentType = "stat_group_header") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Text(
                            text = group.groupLabel ?: "",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                itemsIndexed(
                    items = group.items.orEmpty(),
                    key = { index, it -> "stat_${group.groupLabel ?: ""}_${it.incidentName ?: ""}_$index" },
                    contentType = { _, _ -> "stat_row" }
                ) { index, stat ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp)
                    ) {
                        StatProgressRow(stat = stat)
                    }
                }
                
                item(key = "group_footer_${group.groupLabel ?: group.hashCode()}", contentType = "stat_group_footer") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                            .padding(bottom = 16.dp)
                    ) {
                        // Footer padding for the card illusion
                    }
                }
            }
        }
    }
}

@Composable
private fun StageToggleBar(
    stages: List<EventStatsStage>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        stages.forEachIndexed { index, stage ->
            val isSelected = index == selectedIndex
            // Map JSON names to Sofascore UI labels
            val tabName = when (stage.stageName) {
                "Match" -> "ALL"
                "1st Half" -> "1ST"
                "2nd Half" -> "2ND"
                else -> stage.stageName?.uppercase() ?: ""
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabName,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun StatProgressRow(stat: EventStatsItem) {
    val soccerColors = LocalSoccerColors.current
    val HomeColor = soccerColors.statsHome
    val AwayColor = soccerColors.statsAway

    val homeStr = stat.valueHome ?: "0"
    val awayStr = stat.valueAway ?: "0"

    // Sofascore differentiates styling between percentages and absolute numbers
    val isPercentage = homeStr.contains("%") || awayStr.contains("%")

    // Parse floats safely (strip '%' if present)
    val homeVal = homeStr.replace("%", "").toFloatOrNull() ?: 0f
    val awayVal = awayStr.replace("%", "").toFloatOrNull() ?: 0f

    val highIsBetter = isHighBetter(stat.incidentName ?: "")

    val isHomeWorse = if (highIsBetter) homeVal < awayVal else homeVal > awayVal
    val isAwayWorse = if (highIsBetter) awayVal < homeVal else awayVal > homeVal

    val homeBarColor = if (isHomeWorse && homeVal != awayVal) HomeColor.copy(alpha = 0.3f) else HomeColor
    val awayBarColor = if (isAwayWorse && homeVal != awayVal) AwayColor.copy(alpha = 0.3f) else AwayColor

    // Use absolute values to handle negative stats safely (e.g., "Goals prevented" -> -1.07)
    val homeAbs = abs(homeVal)
    val awayAbs = abs(awayVal)
    val totalAbs = homeAbs + awayAbs

    // Calculate fractions for the bars
    val homeFrac = if (totalAbs > 0f) homeAbs / totalAbs else 0f
    val awayFrac = if (totalAbs > 0f) awayAbs / totalAbs else 0f

    val homeAnim by animateFloatAsState(targetValue = homeFrac, animationSpec = tween(600), label = "homeAnim")
    val awayAnim by animateFloatAsState(targetValue = awayFrac, animationSpec = tween(600), label = "awayAnim")

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {

        // ─── 1. Header Values & Labels ───
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Value
            if (isPercentage) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(HomeColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = homeStr, color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = homeStr,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            // Stat Title (Center)
            Text(
                text = stat.incidentName ?: "",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // Muted dark gray
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(if (isPercentage) 1.5f else 2f)
            )

            // Away Value
            if (isPercentage) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AwayColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = awayStr, color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = awayStr,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ─── 2. Progress Bars ───
        if (isPercentage) {
            // Continuous connected bar for percentages (e.g., Ball Possession 53% | 47%)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Box(modifier = Modifier.weight(homeAnim.coerceAtLeast(0.01f)).fillMaxHeight().background(HomeColor))
                Box(modifier = Modifier.weight(awayAnim.coerceAtLeast(0.01f)).fillMaxHeight().background(AwayColor))
            }
        } else {
            // Split, center-anchored bars for absolute counts (e.g., Total Shots 9 vs 14)
            Row(
                modifier = Modifier.fillMaxWidth().height(4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // Home Bar (Right-aligned inside its left-half bounds)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp)),
                    contentAlignment = Alignment.CenterEnd // Anchors growth to the center
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(HomeColor.copy(alpha = 0.2f)))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = homeAnim)
                            .fillMaxHeight()
                            .background(homeBarColor)
                    )
                }

                // Center Gap
                Spacer(modifier = Modifier.width(4.dp))

                // Away Bar (Left-aligned inside its right-half bounds)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)),
                    contentAlignment = Alignment.CenterStart // Anchors growth to the center
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(AwayColor.copy(alpha = 0.2f)))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = awayAnim)
                            .fillMaxHeight()
                            .background(awayBarColor)
                    )
                }
            }
        }
    }
}
