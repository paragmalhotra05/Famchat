package clone.arattai.famchat

import android.app.Application
import com.google.crypto.tink.config.TinkConfig
import clone.arattai.famchat.util.BackupManager

class FamChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TinkConfig.register()
        BackupManager.scheduleBackup(this)
    }
}
