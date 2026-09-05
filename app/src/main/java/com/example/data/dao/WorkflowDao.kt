package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClientOrder
import com.example.data.model.CodeArtifact
import com.example.data.model.ProjectTask
import com.example.data.model.QaCheck
import com.example.data.model.WhatsAppMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkflowDao {

    // Orders
    @Query("SELECT * FROM client_orders ORDER BY updatedAt DESC")
    fun getAllOrders(): Flow<List<ClientOrder>>

    @Query("SELECT * FROM client_orders ORDER BY updatedAt DESC")
    suspend fun getAllOrdersDirect(): List<ClientOrder>

    @Query("SELECT * FROM client_orders WHERE id = :orderId")
    fun getOrderById(orderId: Long): Flow<ClientOrder?>

    @Query("SELECT * FROM client_orders WHERE id = :orderId")
    suspend fun getOrderByIdDirect(orderId: Long): ClientOrder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: ClientOrder): Long

    @Update
    suspend fun updateOrder(order: ClientOrder)

    @Query("DELETE FROM client_orders WHERE id = :orderId")
    suspend fun deleteOrder(orderId: Long)

    // Tasks
    @Query("SELECT * FROM project_tasks WHERE orderId = :orderId ORDER BY orderIndex ASC")
    fun getTasksForOrder(orderId: Long): Flow<List<ProjectTask>>

    @Query("SELECT * FROM project_tasks WHERE orderId = :orderId ORDER BY orderIndex ASC")
    suspend fun getTasksForOrderDirect(orderId: Long): List<ProjectTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<ProjectTask>)

    @Update
    suspend fun updateTask(task: ProjectTask)

    @Query("UPDATE project_tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean)

    // Code Artifacts
    @Query("SELECT * FROM code_artifacts WHERE orderId = :orderId")
    fun getCodeArtifactsForOrder(orderId: Long): Flow<List<CodeArtifact>>

    @Query("SELECT * FROM code_artifacts WHERE orderId = :orderId")
    suspend fun getCodeArtifactsForOrderDirect(orderId: Long): List<CodeArtifact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCodeArtifacts(artifacts: List<CodeArtifact>)

    // QA Checks
    @Query("SELECT * FROM qa_checks WHERE orderId = :orderId")
    fun getQaChecksForOrder(orderId: Long): Flow<List<QaCheck>>

    @Query("SELECT * FROM qa_checks WHERE orderId = :orderId")
    suspend fun getQaChecksForOrderDirect(orderId: Long): List<QaCheck>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQaChecks(checks: List<QaCheck>)

    @Update
    suspend fun updateQaCheck(check: QaCheck)

    // WhatsApp Messages
    @Query("SELECT * FROM whatsapp_messages ORDER BY timestamp DESC")
    fun getAllWhatsAppMessages(): Flow<List<WhatsAppMessage>>

    @Query("SELECT * FROM whatsapp_messages WHERE orderId = :orderId ORDER BY timestamp DESC")
    fun getWhatsAppMessagesForOrder(orderId: Long): Flow<List<WhatsAppMessage>>

    @Query("SELECT * FROM whatsapp_messages WHERE orderId = :orderId ORDER BY timestamp DESC")
    suspend fun getWhatsAppMessagesForOrderDirect(orderId: Long): List<WhatsAppMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhatsAppMessage(message: WhatsAppMessage): Long

    // Gemini Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<com.example.data.model.ChatMessage>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAllChatMessagesDirect(): List<com.example.data.model.ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: com.example.data.model.ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()
}

