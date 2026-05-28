package com.example.soccerworld.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.soccerworld.R

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object LeagueSelection : Screen("league_selection")
    object Main : Screen("main")
    object MatchDetail : Screen("match_detail/{fixture_id}") {
        fun createRoute(fixtureId: String) = "match_detail/$fixtureId"
    }
    object TeamDetail : Screen("team_detail/{team_id}") {
        fun createRoute(teamId: String) = "team_detail/$teamId"
    }
    object PlayerDetail : Screen("player_detail/{player_id}") {
        fun createRoute(playerId: String) = "player_detail/$playerId"
    }
    object NotificationSettings : Screen("notification_settings")
}

sealed class BottomNavItem(val route: String, val titleResId: Int, val icon: ImageVector) {
    object Matches : BottomNavItem("matches", R.string.nav_matches, Icons.Default.DateRange)
    object Search : BottomNavItem("search", R.string.nav_search, Icons.Default.Search)
    object Favorites : BottomNavItem("favorites", R.string.nav_favorites, Icons.Default.Favorite)
    object Profile : BottomNavItem("profile", R.string.nav_profile, Icons.Default.Person)
}
