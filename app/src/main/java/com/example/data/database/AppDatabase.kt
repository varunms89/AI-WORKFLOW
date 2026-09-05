package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.WorkflowDao
import com.example.data.model.ChatMessage
import com.example.data.model.ClientOrder
import com.example.data.model.CodeArtifact
import com.example.data.model.ProjectTask
import com.example.data.model.QaCheck
import com.example.data.model.WhatsAppMessage

@Database(
    entities = [
        ClientOrder::class,
        ProjectTask::class,
        CodeArtifact::class,
        QaCheck::class,
        WhatsAppMessage::class,
        ChatMessage::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workflowDao(): WorkflowDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "order_workflow_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
