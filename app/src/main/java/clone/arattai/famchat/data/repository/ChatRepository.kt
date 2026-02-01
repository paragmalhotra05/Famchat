package clone.arattai.famchat.data.repository

import clone.arattai.famchat.data.local.ChatDao
import clone.arattai.famchat.data.model.Conversation
import clone.arattai.famchat.data.model.Message
import clone.arattai.famchat.data.model.User
import clone.arattai.famchat.util.KeyManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository(private val chatDao: ChatDao) {
    private val firestore = FirebaseFirestore.getInstance()

    fun getConversations() = chatDao.getConversations()

    fun getMessages(chatId: String) = chatDao.getMessages(chatId)

    suspend fun sendMessage(senderId: String, receiverId: String, content: String) {
        val receiverDoc = firestore.collection("users").document(receiverId).get().await()
        val receiver = receiverDoc.toObject(User::class.java)

        if (receiver == null || receiver.publicKey.isEmpty()) {
            throw Exception("Receiver not found or E2EE not supported by receiver")
        }

        val encryptedContent = KeyManager.encrypt(content, receiver.publicKey)

        val messageId = firestore.collection("messages").document().id
        val message = Message(
            id = messageId,
            chatId = getChatId(senderId, receiverId),
            senderId = senderId,
            receiverId = receiverId,
            content = encryptedContent,
            timestamp = System.currentTimeMillis()
        )

        chatDao.insertMessage(message.copy(content = content))

        chatDao.insertConversation(Conversation(
            id = receiverId,
            otherUserDisplayName = receiver.displayName,
            otherUserProfileUrl = receiver.profileImageUrl,
            lastMessage = content,
            lastMessageTimestamp = message.timestamp
        ))

        firestore.collection("messages").document(messageId).set(message).await()
    }

    fun listenForMessages(userId: String): Flow<Message> = callbackFlow {
        val registration = firestore.collection("messages")
            .whereEqualTo("receiverId", userId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val msg = change.document.toObject(Message::class.java)
                        trySend(msg)
                    }
                }
            }
        awaitClose { registration.remove() }
    }

    private fun getChatId(u1: String, u2: String): String {
        return if (u1 < u2) "${u1}_$u2" else "${u2}_$u1"
    }
}
