package com.example.soccerworld.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.soccerworld.ui.favorites.FavoritesScreen
import com.example.soccerworld.ui.home.HomeScreen
import com.example.soccerworld.ui.fixture.FixturesScreen
import com.example.soccerworld.ui.navigation.Screen
import com.example.soccerworld.ui.team.TeamsScreen
import com.example.soccerworld.ui.navigation.BottomNavItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(rootNavController: NavHostController = rememberNavController()) {
    val navController = rememberNavController()
    
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Fixtures,
        BottomNavItem.Teams,
        BottomNavItem.Favorites
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Soccer World", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = {
                        rootNavController.navigate(Screen.LeagueSelection.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Chọn giải đấu",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
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
                HomeScreen()
            }
            composable(BottomNavItem.Fixtures.route) {
                FixturesScreen(onMatchClick = { matchId ->
                    rootNavController.navigate(Screen.MatchDetail.createRoute(matchId))
                })
            }
            composable(BottomNavItem.Teams.route) {
                TeamsScreen(onTeamClick = { teamId ->
                    rootNavController.navigate(Screen.TeamDetail.createRoute(teamId))
                })
            }
            composable(BottomNavItem.Favorites.route) {
                FavoritesScreen(onMatchClick = { matchId ->
                    rootNavController.navigate(Screen.MatchDetail.createRoute(matchId))
                })
            }
        }
    }
}
