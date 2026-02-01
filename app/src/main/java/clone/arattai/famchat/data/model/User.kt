package clone.arattai.famchat.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val status: String = "Hey there! I am using FamChat.",
    val publicKey: String = "" // For E2EE
)
