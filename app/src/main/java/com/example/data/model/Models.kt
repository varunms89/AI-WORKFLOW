package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "client_orders")
data class ClientOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientName: String,
    val projectTitle: String,
    val rawBrief: String,
    val brandAssets: String,
    val deadline: String,
    val targetAudience: String = "",
    val coreObjectives: String = "",
    val requiredFeatures: String = "",
    val designPreferences: String = "",
    val ambiguitiesAndAssumptions: String = "",
    val currentPhase: Int = 1, // 1: Intake, 2: Scheduling, 3: Execution, 4: QA, 5: Submission
    val status: String = "INTAKE", // INTAKE, IN_PROGRESS, BOTTLENECK_ALERT, QA_REVIEW, COMPLETED
    val hasBottleneck: Boolean = false,
    val bottleneckDescription: String = "",
    val bottleneckMitigation: String = "",
    val livePreviewUrl: String = "",
    val deploymentInstructions: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "project_tasks")
data class ProjectTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val phase: Int,
    val title: String,
    val category: String, // Architecture, Design, Frontend, Backend, Testing, DevOps
    val estimatedDuration: String,
    val milestoneTime: String,
    val dependencies: String = "None",
    val isCompleted: Boolean = false,
    val executionOutputSnippet: String = "",
    val orderIndex: Int = 0
)

@Entity(tableName = "code_artifacts")
data class CodeArtifact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val fileName: String,
    val language: String, // html, css, typescript, javascript, json
    val code: String,
    val description: String
)

@Entity(tableName = "qa_checks")
data class QaCheck(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val category: String, // Code Validity, Link Integrity, Asset Optimization, Accessibility (WCAG 2.1 AA), Scope Alignment
    val title: String,
    val details: String,
    val status: String = "PASSED", // PASSED, WARNING, FAILED
    val remediationNote: String = ""
)

@Entity(tableName = "whatsapp_messages")
data class WhatsAppMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val orderTitle: String,
    val phase: Int,
    val recipientNumber: String,
    val messageType: String, // INTAKE_CONFIRMATION, SCHEDULE_DISPATCH, TASK_UPDATE, BOTTLENECK_ALERT, FINAL_DELIVERY
    val timestamp: Long = System.currentTimeMillis(),
    val content: String,
    val status: String = "DELIVERED" // SENT, DELIVERED, READ
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user" or "model"
    val text: String,
    val modelName: String = "models/gemini-3.8-flash",
    val rolePreset: String = "Workflow Strategist",
    val timestamp: Long = System.currentTimeMillis()
)

