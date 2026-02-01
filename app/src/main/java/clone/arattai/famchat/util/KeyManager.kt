package clone.arattai.famchat.util

import android.content.Context
import android.util.Base64
import com.google.crypto.tink.HybridDecrypt
import com.google.crypto.tink.HybridEncrypt
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.hybrid.HybridConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.io.ByteArrayOutputStream

object KeyManager {
    private const val KEYSET_NAME = "famchat_keyset"
    private const val PREF_FILE_NAME = "famchat_prefs"
    private const val MASTER_KEY_URI = "android-keystore://famchat_master_key"

    init {
        HybridConfig.register()
    }

    fun getKeysetHandle(context: Context): KeysetHandle {
        return AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
            .withKeyTemplate(KeyTemplates.get("ECIES_P256_HKDF_HMAC_SHA256_AES128_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
    }

    fun getPublicKeyString(keysetHandle: KeysetHandle): String {
        val outputStream = ByteArrayOutputStream()
        keysetHandle.publicKeysetHandle.writeNoSecret(com.google.crypto.tink.BinaryKeysetWriter.withOutputStream(outputStream))
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    fun encrypt(plainText: String, publicKeyString: String): String {
        val publicKeysetHandle = KeysetHandle.readNoSecret(
            com.google.crypto.tink.BinaryKeysetReader.withBytes(Base64.decode(publicKeyString, Base64.NO_WRAP))
        )
        val hybridEncrypt = publicKeysetHandle.getPrimitive(HybridEncrypt::class.java)
        val ciphertext = hybridEncrypt.encrypt(plainText.toByteArray(), null)
        return Base64.encodeToString(ciphertext, Base64.NO_WRAP)
    }

    fun decrypt(encryptedText: String, keysetHandle: KeysetHandle): String {
        val hybridDecrypt = keysetHandle.getPrimitive(HybridDecrypt::class.java)
        val ciphertext = Base64.decode(encryptedText, Base64.NO_WRAP)
        val decrypted = hybridDecrypt.decrypt(ciphertext, null)
        return String(decrypted)
    }
}
