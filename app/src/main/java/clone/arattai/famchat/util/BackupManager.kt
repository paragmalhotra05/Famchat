package clone.arattai.famchat.util

import android.content.Context
import androidx.work.*
import clone.arattai.famchat.worker.BackupWorker
import java.util.concurrent.TimeUnit

object BackupManager {
    fun scheduleBackup(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "FamChatBackup",
            ExistingPeriodicWorkPolicy.KEEP,
            backupRequest
        )
    }
}
