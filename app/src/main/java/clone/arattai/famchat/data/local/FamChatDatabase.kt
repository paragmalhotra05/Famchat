package clone.arattai.famchat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import clone.arattai.famchat.data.model.Conversation
import clone.arattai.famchat.data.model.Message

@Database(entities = [Message::class, Conversation::class], version = 1, exportSchema = false)
abstract class FamChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: FamChatDatabase? = null

        fun getDatabase(context: Context): FamChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FamChatDatabase::class.java,
                    "famchat_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
