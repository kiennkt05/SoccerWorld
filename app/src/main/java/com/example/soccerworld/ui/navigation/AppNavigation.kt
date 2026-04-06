package com.example.soccerworld.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.soccerworld.ui.main.MainScreen
import com.example.soccerworld.ui.onboarding.LeagueSelectionScreen
import com.example.soccerworld.ui.fixture.detail.MatchDetailScreen
import com.example.soccerworld.ui.team.team_detail.TeamDetailScreen
import com.example.soccerworld.util.CustomSharedPreferences

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val sharedPrefs = CustomSharedPreferences.invoke(context)
    val navController = rememberNavController()

    val startDestination = if (sharedPrefs.hasSelectedLeague()) {
        Screen.Main.route
    } else {
        Screen.LeagueSelection.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.LeagueSelection.route) {
            LeagueSelectionScreen(
                onLeagueSelected = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.LeagueSelection.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Main.route) {
            MainScreen(rootNavController = navController)
        }
        composable(
            route = Screen.MatchDetail.route,
            arguments = listOf(navArgument("fixture_id") { type = NavType.IntType })
        ) { backStackEntry ->
            val fixtureId = backStackEntry.arguments?.getInt("fixture_id") ?: return@composable
            MatchDetailScreen(fixtureId = fixtureId, onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.TeamDetail.route,
            arguments = listOf(navArgument("team_id") { type = NavType.IntType })
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getInt("team_id") ?: return@composable
            TeamDetailScreen(teamId = teamId, onBack = { navController.popBackStack() })
        }
    }
}
