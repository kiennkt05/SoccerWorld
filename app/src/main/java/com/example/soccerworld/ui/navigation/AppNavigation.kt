package com.example.soccerworld.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.soccerworld.ui.auth.AuthViewModel
import com.example.soccerworld.ui.auth.LoginScreen
import com.example.soccerworld.ui.main.MainScreen
import com.example.soccerworld.ui.onboarding.LeagueSelectionScreen
import com.example.soccerworld.ui.fixture.detail.MatchDetailScreen
import com.example.soccerworld.ui.team.team_detail.TeamDetailScreen
import com.example.soccerworld.ui.player.PlayerDetailScreen
import com.example.soccerworld.ui.player.PlayerDetailInfo
import com.example.soccerworld.util.CustomSharedPreferences

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val sharedPrefs = CustomSharedPreferences.invoke(context)
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

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
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Main.route) {
            MainScreen(rootNavController = navController)
        }
        composable(
            route = Screen.MatchDetail.route,
            arguments = listOf(navArgument("fixture_id") { type = NavType.StringType })
        ) { backStackEntry ->
            val fixtureId = backStackEntry.arguments?.getString("fixture_id") ?: return@composable
            MatchDetailScreen(
                fixtureId = fixtureId,
                onBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(
            route = Screen.TeamDetail.route,
            arguments = listOf(navArgument("team_id") { type = NavType.StringType })
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString("team_id") ?: return@composable
            TeamDetailScreen(
                teamId = teamId,
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { playerInfo ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("player_info", playerInfo)
                    navController.navigate(Screen.PlayerDetail.createRoute(playerInfo.id))
                }
            )
        }
        composable(
            route = Screen.PlayerDetail.route,
            arguments = listOf(navArgument("player_id") { type = NavType.StringType })
        ) {
            val playerInfo = remember {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<PlayerDetailInfo>("player_info")
            }
            if (playerInfo != null) {
                PlayerDetailScreen(
                    playerInfo = playerInfo,
                    onBack = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
