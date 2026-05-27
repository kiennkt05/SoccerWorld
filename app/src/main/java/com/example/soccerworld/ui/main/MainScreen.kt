package com.example.soccerworld.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.soccerworld.ui.auth.AuthViewModel
import com.example.soccerworld.ui.favorites.FavoritesScreen
import com.example.soccerworld.ui.matches.MatchesScreen
import com.example.soccerworld.ui.navigation.Screen
import com.example.soccerworld.ui.search.SearchScreen
import com.example.soccerworld.ui.profile.ProfileScreen
import com.example.soccerworld.ui.navigation.BottomNavItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.soccerworld.data.ChatRepository
import com.example.soccerworld.data.local.FootballDatabase
import com.example.soccerworld.data.remote.groq.GroqApiClient
import com.example.soccerworld.ui.chatbot.ChatBottomSheet
import com.example.soccerworld.ui.chatbot.ChatFab
import com.example.soccerworld.ui.chatbot.ChatViewModel
import com.example.soccerworld.util.ViewModelFactory
import com.example.soccerworld.data.agent.ToolExecutor
import com.example.soccerworld.util.Injection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset
import com.example.soccerworld.ui.theme.AccentEmerald
import com.example.soccerworld.ui.theme.TextSecondary
import com.example.soccerworld.util.CustomSharedPreferences

@Composable
fun MainScreen(rootNavController: NavHostController = rememberNavController()) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    
    var showChat by remember { mutableStateOf(false) }
    
    val items = listOf(
        BottomNavItem.Matches,
        BottomNavItem.Search,
        BottomNavItem.Favorites,
        BottomNavItem.Profile
    )

    Scaffold(
        floatingActionButton = {
            ChatFab(onClick = { showChat = true })
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { 
                            Text(
                                text = item.title,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                navController.graph.startDestinationRoute?.let { route ->
                                    popUpTo(route) { saveState = true }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = TextSecondary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Matches.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Matches.route) {
                MatchesScreen(
                    onTeamClick = { teamId ->
                        rootNavController.navigate(Screen.TeamDetail.createRoute(teamId))
                    },
                    onChangeLeague = {
                        rootNavController.navigate(Screen.LeagueSelection.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    },
                    onMatchClick = { matchId ->
                        rootNavController.navigate(Screen.MatchDetail.createRoute(matchId))
                    }
                )
            }
            composable(BottomNavItem.Search.route) {
                val context = LocalContext.current
                SearchScreen(
                    onTeamClick = { teamId ->
                        rootNavController.navigate(Screen.TeamDetail.createRoute(teamId))
                    },
                    onPlayerClick = { playerId ->
                        rootNavController.navigate(Screen.PlayerDetail.createRoute(playerId))
                    },
                    onCompetitionClick = { leagueCode ->
                        CustomSharedPreferences(context).saveLeagueId(leagueCode)
                        navController.navigate(BottomNavItem.Matches.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomNavItem.Favorites.route) {
                FavoritesScreen(
                    onMatchClick = { matchId ->
                        rootNavController.navigate(Screen.MatchDetail.createRoute(matchId))
                    },
                    onNavigateToLogin = {
                        rootNavController.navigate(Screen.Login.route)
                    },
                    onTeamClick = { teamId ->
                        rootNavController.navigate(Screen.TeamDetail.createRoute(teamId))
                    }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onChangeLeague = {
                        rootNavController.navigate(Screen.LeagueSelection.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        rootNavController.navigate(Screen.Login.route)
                    },
                    authViewModel = authViewModel
                )
            }
        }
    }

    if (showChat) {
        val context = LocalContext.current
        val db = FootballDatabase.invoke(context)
        val footballRepo = remember { Injection.provideFootballRepository(context) }
        val toolExecutor = remember { ToolExecutor(footballRepo) }
        val chatRepo = remember { ChatRepository(db.chatDao(), GroqApiClient.api, toolExecutor, context) }
        val chatViewModel: ChatViewModel = viewModel(factory = ViewModelFactory(chatRepository = chatRepo))

        ChatBottomSheet(
            viewModel = chatViewModel,
            onDismiss = { showChat = false }
        )
    }
}
