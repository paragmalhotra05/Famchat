package clone.arattai.famchat.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import clone.arattai.famchat.ui.navigation.Screen
import clone.arattai.famchat.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsTab(navController: NavHostController, authViewModel: AuthViewModel = viewModel()) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    Column(modifier = Modifier.fillMaxSize()) {
        // Profile Section
        Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(user?.displayName ?: "User", style = MaterialTheme.typography.headlineSmall)
                    Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(icon = Icons.Default.Brightness6, title = "Theme") {}

        SettingsItem(icon = Icons.Default.CloudQueue, title = "Connect Google Account (Backup)") {
            // Logic to launch Google Sign-In intent
        }

        SettingsItem(icon = Icons.Default.Backup, title = "Google Drive Backup Settings") {}

        SettingsItem(icon = Icons.Default.Notifications, title = "Notifications") {}

        HorizontalDivider()

        SettingsItem(icon = Icons.AutoMirrored.Filled.ExitToApp, title = "Logout", tint = MaterialTheme.colorScheme.error) {
            authViewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(0)
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tint)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
