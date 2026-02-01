package clone.arattai.famchat.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.android.gms.auth.api.signin.GoogleSignIn
import java.io.File as JavaFile

class BackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val dbFile = applicationContext.getDatabasePath("famchat_database")
            if (dbFile.exists()) {
                uploadToDrive(dbFile)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun uploadToDrive(dbFile: JavaFile) {
        val account = GoogleSignIn.getLastSignedInAccount(applicationContext) ?: return

        val credential = GoogleAccountCredential.usingOAuth2(
            applicationContext, listOf(DriveScopes.DRIVE_FILE)
        ).apply {
            selectedAccount = account.account
        }

        val driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            credential
        ).setApplicationName("FamChat").build()

        // Check if backup already exists to update it or create new
        val existingFiles = driveService.files().list()
            .setQ("name = 'famchat_backup.db' and trashed = false")
            .setSpaces("drive")
            .execute()
            .files

        val fileMetadata = File().apply {
            name = "famchat_backup.db"
        }
        val mediaContent = FileContent("application/octet-stream", dbFile)

        if (existingFiles.isNullOrEmpty()) {
            driveService.files().create(fileMetadata, mediaContent).execute()
        } else {
            val fileId = existingFiles[0].id
            driveService.files().update(fileId, null, mediaContent).execute()
        }
    }
}
