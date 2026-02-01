package clone.arattai.famchat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import clone.arattai.famchat.data.local.FamChatDatabase
import clone.arattai.famchat.data.model.Conversation
import clone.arattai.famchat.data.model.Message
import clone.arattai.famchat.data.model.User
import clone.arattai.famchat.data.repository.ChatRepository
import clone.arattai.famchat.util.KeyManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FamChatDatabase.getDatabase(application)
    private val repository = ChatRepository(db.chatDao())
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val conversations: StateFlow<List<Conversation>> = MutableStateFlow(emptyList())
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        observeConversations()
        fetchUsers()
        startListeningForMessages()
    }

    private fun observeConversations() {
        viewModelScope.launch {
            repository.getConversations().collect {
                (conversations as MutableStateFlow).value = it
            }
        }
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").get().await()
                val userList = snapshot.toObjects(User::class.java).filter { it.uid != auth.currentUser?.uid }
                _users.value = userList
            } catch (e: Exception) {
            }
        }
    }

    private fun startListeningForMessages() {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val keysetHandle = KeyManager.getKeysetHandle(getApplication())
            repository.listenForMessages(currentUserId).collect { message ->
                val decryptedContent = try {
                    KeyManager.decrypt(message.content, keysetHandle)
                } catch (e: Exception) {
                    "[Decryption Error]"
                }

                val decryptedMessage = message.copy(content = decryptedContent)
                db.chatDao().insertMessage(decryptedMessage)

                val senderDoc = firestore.collection("users").document(message.senderId).get().await()
                val sender = senderDoc.toObject(User::class.java)

                db.chatDao().insertConversation(Conversation(
                    id = message.senderId,
                    otherUserDisplayName = sender?.displayName ?: "Unknown",
                    otherUserProfileUrl = sender?.profileImageUrl ?: "",
                    lastMessage = decryptedContent,
                    lastMessageTimestamp = message.timestamp
                ))
            }
        }
    }

    fun getMessages(chatId: String): StateFlow<List<Message>> {
        val flow = MutableStateFlow<List<Message>>(emptyList())
        viewModelScope.launch {
            repository.getMessages(chatId).collect {
                flow.value = it
            }
        }
        return flow
    }

    fun sendMessage(receiverId: String, content: String) {
        val senderId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.sendMessage(senderId, receiverId, content)
            } catch (e: Exception) {
            }
        }
    }
}
