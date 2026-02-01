package clone.arattai.famchat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "", // This will be encrypted
    val timestamp: Long = 0,
    val type: String = "text",
    val status: String = "sent" // sent, delivered, read
)

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String = "", // Usually the other user's UID for 1-on-1
    val otherUserDisplayName: String = "",
    val otherUserProfileUrl: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0,
    val unreadCount: Int = 0
)
