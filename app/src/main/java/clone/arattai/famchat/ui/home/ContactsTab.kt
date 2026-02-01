package clone.arattai.famchat.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import clone.arattai.famchat.data.model.User
import clone.arattai.famchat.ui.navigation.Screen
import clone.arattai.famchat.viewmodel.ChatViewModel
import coil.compose.AsyncImage

@Composable
fun ContactsTab(navController: NavHostController, viewModel: ChatViewModel = viewModel()) {
    val users by viewModel.users.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(users) { user ->
            ContactItem(user) {
                navController.navigate(Screen.Chat.createRoute(user.uid))
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 72.dp), thickness = 0.5.dp, color = Color.LightGray)
        }
    }
}

@Composable
fun ContactItem(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.profileImageUrl.ifEmpty { "https://ui-avatars.com/api/?name=${user.displayName}&background=008080&color=fff" },
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = user.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = user.status,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}
