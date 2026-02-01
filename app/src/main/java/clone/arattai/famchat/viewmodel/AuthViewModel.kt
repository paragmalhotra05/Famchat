package clone.arattai.famchat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import clone.arattai.famchat.data.model.User
import clone.arattai.famchat.util.KeyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    val userState: StateFlow<UserState> = _userState

    val currentUser: com.google.firebase.auth.FirebaseUser?
        get() = auth.currentUser

    fun signUp(email: String, pass: String, displayName: String) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: throw Exception("User creation failed")

                // Generate and persist KeysetHandle
                val keysetHandle = KeyManager.getKeysetHandle(getApplication())
                val publicKeyString = KeyManager.getPublicKeyString(keysetHandle)

                val newUser = User(
                    uid = uid,
                    email = email,
                    displayName = displayName,
                    publicKey = publicKeyString
                )

                firestore.collection("users").document(uid).set(newUser).await()
                _userState.value = UserState.Success(newUser)
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                val uid = auth.currentUser?.uid ?: throw Exception("Login failed")
                val doc = firestore.collection("users").document(uid).get().await()
                val user = doc.toObject(User::class.java)

                if (user != null) {
                    if (user.publicKey.isEmpty()) {
                        val keysetHandle = KeyManager.getKeysetHandle(getApplication())
                        val publicKeyString = KeyManager.getPublicKeyString(keysetHandle)
                        firestore.collection("users").document(uid).update("publicKey", publicKeyString)
                        _userState.value = UserState.Success(user.copy(publicKey = publicKeyString))
                    } else {
                        _userState.value = UserState.Success(user)
                    }
                } else {
                    _userState.value = UserState.Error("User data not found")
                }
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _userState.value = UserState.Idle
    }
}

sealed class UserState {
    object Idle : UserState()
    object Loading : UserState()
    data class Success(val user: User) : UserState()
    data class Error(val message: String) : UserState()
}
