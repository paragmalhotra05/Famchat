package clone.arattai.famchat.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import clone.arattai.famchat.ui.settings.SettingsTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(rootNavController: NavHostController) {
    val homeNavController = rememberNavController()
    val items = listOf(
        HomeTab.Chats,
        HomeTab.Contacts,
        HomeTab.Settings
    )

    Scaffold(
        topBar = {
            val navBackStackEntry by homeNavController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val title = items.find { it.route == currentRoute }?.title ?: "FamChat"
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by homeNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = currentRoute == tab.route,
                        onClick = {
                            homeNavController.navigate(tab.route) {
                                homeNavController.graph.startDestinationRoute?.let { route ->
                                    popUpTo(route) {
                                        saveState = true
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = homeNavController,
            startDestination = HomeTab.Chats.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(HomeTab.Chats.route) { ChatsTab(rootNavController) }
            composable(HomeTab.Contacts.route) { ContactsTab(rootNavController) }
            composable(HomeTab.Settings.route) { SettingsTab(rootNavController) }
        }
    }
}

sealed class HomeTab(val route: String, val title: String, val icon: ImageVector) {
    object Chats : HomeTab("chats", "Chats", Icons.Default.Chat)
    object Contacts : HomeTab("contacts", "Contacts", Icons.Default.Contacts)
    object Settings : HomeTab("settings", "Settings", Icons.Default.Settings)
}
