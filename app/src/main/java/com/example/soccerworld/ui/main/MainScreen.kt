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
import com.example.soccerworld.ui.home.HomeScreen
import com.example.soccerworld.ui.fixture.FixturesScreen
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

@Composable
fun MainScreen(rootNavController: NavHostController = rememberNavController()) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    
    var showChat by remember { mutableStateOf(false) }
    
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Fixtures,
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
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(text = item.title) },
                        selected = currentRoute == item.route,
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
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(onTeamClick = { teamId ->
                    rootNavController.navigate(Screen.TeamDetail.createRoute(teamId))
                })
            }
            composable(BottomNavItem.Fixtures.route) {
                FixturesScreen(onMatchClick = { matchId ->
                    rootNavController.navigate(Screen.MatchDetail.createRoute(matchId))
                })
            }
            composable(BottomNavItem.Search.route) {
                SearchScreen(
                    onTeamClick = { teamId ->
                        rootNavController.navigate(Screen.TeamDetail.createRoute(teamId))
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
        val chatRepo = remember { ChatRepository(db.chatDao(), GroqApiClient.api, context) }
        val chatViewModel: ChatViewModel = viewModel(factory = ViewModelFactory(chatRepository = chatRepo))

        ChatBottomSheet(
            viewModel = chatViewModel,
            onDismiss = { showChat = false }
        )
    }
}
