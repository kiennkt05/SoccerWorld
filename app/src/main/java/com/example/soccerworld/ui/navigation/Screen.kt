package com.example.soccerworld.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object LeagueSelection : Screen("league_selection")
    object Main : Screen("main")
    object MatchDetail : Screen("match_detail/{fixture_id}") {
        fun createRoute(fixtureId: String) = "match_detail/$fixtureId"
    }
    object TeamDetail : Screen("team_detail/{team_id}") {
        fun createRoute(teamId: String) = "team_detail/$teamId"
    }
}

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "League", Icons.Default.Home)
    object Fixtures : BottomNavItem("fixtures", "Fixtures", Icons.Default.DateRange)
    object Teams : BottomNavItem("teams", "Teams", Icons.AutoMirrored.Filled.List)
    object Favorites : BottomNavItem("favorites", "Favorites", Icons.Default.Favorite)
}
