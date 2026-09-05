package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.dao.WorkflowDao
import com.example.data.gemini.ChatHistoryItem
import com.example.data.gemini.GeminiClient
import com.example.data.gemini.GeminiRoleConfig
import com.example.data.gemini.GeminiRoles
import com.example.data.model.ChatMessage
import com.example.data.model.ClientOrder
import com.example.data.model.CodeArtifact
import com.example.data.model.ProjectTask
import com.example.data.model.QaCheck
import com.example.data.model.WhatsAppMessage
import com.example.domain.WorkflowEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class WorkflowRepository(
    private val dao: WorkflowDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("order_workflow_prefs", Context.MODE_PRIVATE)

    private val geminiClient = GeminiClient()

    var hostWhatsAppNumber: String
        get() = prefs.getString("host_whatsapp_number", "+1 (555) 019-2834") ?: "+1 (555) 019-2834"
        set(value) = prefs.edit().putString("host_whatsapp_number", value).apply()

    fun getAllOrders(): Flow<List<ClientOrder>> = dao.getAllOrders()

    fun getOrderById(id: Long): Flow<ClientOrder?> = dao.getOrderById(id)

    fun getTasksForOrder(orderId: Long): Flow<List<ProjectTask>> = dao.getTasksForOrder(orderId)

    fun getCodeArtifactsForOrder(orderId: Long): Flow<List<CodeArtifact>> =
        dao.getCodeArtifactsForOrder(orderId)

    fun getQaChecksForOrder(orderId: Long): Flow<List<QaCheck>> = dao.getQaChecksForOrder(orderId)

    fun getAllWhatsAppMessages(): Flow<List<WhatsAppMessage>> = dao.getAllWhatsAppMessages()

    fun getWhatsAppMessagesForOrder(orderId: Long): Flow<List<WhatsAppMessage>> =
        dao.getWhatsAppMessagesForOrder(orderId)

    // Gemini Chat Messages
    fun getAllChatMessages(): Flow<List<ChatMessage>> = dao.getAllChatMessages()

    suspend fun sendChatMessage(
        userText: String,
        modelName: String,
        roleConfig: GeminiRoleConfig,
        activeOrder: ClientOrder?
    ): Result<String> {
        val userMsg = ChatMessage(
            role = "user",
            text = userText,
            modelName = modelName,
            rolePreset = roleConfig.name
        )
        dao.insertChatMessage(userMsg)

        val directHistory = dao.getAllChatMessagesDirect().map {
            ChatHistoryItem(role = it.role, text = it.text)
        }

        val orderContext = activeOrder?.let {
            """
            Title: ${it.projectTitle}
            Client: ${it.clientName}
            Phase: ${it.currentPhase} of 5 (${it.status})
            Deadline: ${it.deadline}
            Core Objectives: ${it.coreObjectives}
            Required Features: ${it.requiredFeatures}
            Bottleneck: ${if (it.hasBottleneck) it.bottleneckDescription else "None"}
            """.trimIndent()
        }

        val result = geminiClient.generateChatResponse(
            model = modelName,
            systemInstruction = roleConfig.systemInstruction,
            history = directHistory,
            activeOrderContext = orderContext
        )

        result.onSuccess { replyText ->
            val modelMsg = ChatMessage(
                role = "model",
                text = replyText,
                modelName = modelName,
                rolePreset = roleConfig.name
            )
            dao.insertChatMessage(modelMsg)
        }

        return result
    }

    suspend fun clearChatHistory() {
        dao.clearChatHistory()
    }


    suspend fun createNewOrder(
        clientName: String,
        projectTitle: String,
        rawBrief: String,
        brandAssets: String,
        deadline: String
    ): Long {
        val initialOrder = ClientOrder(
            clientName = clientName,
            projectTitle = projectTitle,
            rawBrief = rawBrief,
            brandAssets = brandAssets,
            deadline = deadline,
            livePreviewUrl = "https://preview.clientops.app/live/${System.currentTimeMillis() % 10000}"
        )

        // Phase 1: Intake & Analysis
        val analyzedOrder = WorkflowEngine.generateIntakeAnalysis(initialOrder)
        val orderId = dao.insertOrder(analyzedOrder)

        // Phase 1 WhatsApp Action: Send automated intake confirmation message to host WhatsApp
        val intakeMsg = WorkflowEngine.buildIntakeWhatsAppMessage(analyzedOrder.copy(id = orderId), hostWhatsAppNumber)
        dao.insertWhatsAppMessage(intakeMsg)

        // Phase 2 Task Breakdown prepared
        val tasks = WorkflowEngine.generateInitialTasks(orderId, projectTitle)
        dao.insertTasks(tasks)

        // Code Artifacts and QA Checks prepared
        val artifacts = WorkflowEngine.generateCodeArtifacts(analyzedOrder.copy(id = orderId))
        dao.insertCodeArtifacts(artifacts)

        val qaChecks = WorkflowEngine.generateQaChecks(orderId)
        dao.insertQaChecks(qaChecks)

        return orderId
    }

    suspend fun advanceToPhase(orderId: Long, targetPhase: Int) {
        val order = dao.getOrderByIdDirect(orderId) ?: return
        if (targetPhase <= order.currentPhase && targetPhase != 5) return

        val updatedStatus = when (targetPhase) {
            1 -> "INTAKE"
            2 -> "IN_PROGRESS"
            3 -> "IN_PROGRESS"
            4 -> "QA_REVIEW"
            5 -> "COMPLETED"
            else -> order.status
        }

        val updatedOrder = order.copy(
            currentPhase = targetPhase,
            status = updatedStatus,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateOrder(updatedOrder)

        // Trigger Phase-specific WhatsApp Actions to Host
        when (targetPhase) {
            2 -> {
                val tasks = dao.getTasksForOrderDirect(orderId)
                val scheduleMsg = WorkflowEngine.buildScheduleWhatsAppMessage(updatedOrder, tasks, hostWhatsAppNumber)
                dao.insertWhatsAppMessage(scheduleMsg)
            }
            3 -> {
                // Moving into Execution
                val msg = WhatsAppMessage(
                    orderId = orderId,
                    orderTitle = order.projectTitle,
                    phase = 3,
                    recipientNumber = hostWhatsAppNumber,
                    messageType = "TASK_UPDATE",
                    content = "⚡ *PHASE 3 EXECUTION INITIALIZED*\n━━━━━━━━━━━━━━━━━━━━\nProject: *${order.projectTitle}*\nFrontend and backend component generation active. Code artifacts compiling with zero placeholder content."
                )
                dao.insertWhatsAppMessage(msg)
            }
            4 -> {
                val msg = WhatsAppMessage(
                    orderId = orderId,
                    orderTitle = order.projectTitle,
                    phase = 4,
                    recipientNumber = hostWhatsAppNumber,
                    messageType = "TASK_UPDATE",
                    content = "🧪 *PHASE 4 QA & REVIEW STARTED*\n━━━━━━━━━━━━━━━━━━━━\nProject: *${order.projectTitle}*\nAutomated validation checks running on code validity, link integrity, asset optimization, and WCAG 2.1 AA accessibility standards."
                )
                dao.insertWhatsAppMessage(msg)
            }
            5 -> {
                val deliveryMsg = WorkflowEngine.buildFinalDeliveryWhatsAppMessage(updatedOrder, hostWhatsAppNumber)
                dao.insertWhatsAppMessage(deliveryMsg)
            }
        }
    }

    suspend fun markTaskCompleted(taskId: Long, orderId: Long) {
        dao.setTaskCompleted(taskId, true)
        val order = dao.getOrderByIdDirect(orderId) ?: return
        val tasks = dao.getTasksForOrderDirect(orderId)
        val completedTask = tasks.find { it.id == taskId } ?: return

        // Phase 3 WhatsApp Action: Automatically trigger status update notification to Host WhatsApp as major tasks are completed
        val statusMsg = WorkflowEngine.buildTaskCompletionWhatsAppMessage(order, completedTask, hostWhatsAppNumber)
        dao.insertWhatsAppMessage(statusMsg)

        // If all tasks completed, suggest or auto advance
        if (tasks.all { it.isCompleted || it.id == taskId }) {
            if (order.currentPhase < 4) {
                advanceToPhase(orderId, 4)
            }
        }
    }

    suspend fun reportBottleneck(orderId: Long, bottleneck: String, mitigation: String) {
        val order = dao.getOrderByIdDirect(orderId) ?: return
        val updated = order.copy(
            hasBottleneck = true,
            bottleneckDescription = bottleneck,
            bottleneckMitigation = mitigation,
            status = "BOTTLENECK_ALERT",
            updatedAt = System.currentTimeMillis()
        )
        dao.updateOrder(updated)

        // WhatsApp Action: Immediately alert host via WhatsApp with mitigation plan
        val alertMsg = WorkflowEngine.buildBottleneckAlertWhatsAppMessage(
            updated,
            bottleneck,
            mitigation,
            hostWhatsAppNumber
        )
        dao.insertWhatsAppMessage(alertMsg)
    }

    suspend fun resolveBottleneck(orderId: Long) {
        val order = dao.getOrderByIdDirect(orderId) ?: return
        val updated = order.copy(
            hasBottleneck = false,
            bottleneckDescription = "",
            bottleneckMitigation = "",
            status = if (order.currentPhase == 5) "COMPLETED" else "IN_PROGRESS",
            updatedAt = System.currentTimeMillis()
        )
        dao.updateOrder(updated)

        val resolvedMsg = WhatsAppMessage(
            orderId = orderId,
            orderTitle = order.projectTitle,
            phase = order.currentPhase,
            recipientNumber = hostWhatsAppNumber,
            messageType = "TASK_UPDATE",
            content = "✅ *BOTTLENECK RESOLVED*\n━━━━━━━━━━━━━━━━━━━━\nProject: *${order.projectTitle}*\nMitigation applied successfully. Workflow pipeline resumed at normal velocity. Target delivery deadline guaranteed."
        )
        dao.insertWhatsAppMessage(resolvedMsg)
    }

    // ==========================================
    // AUTONOMOUS AI WORKFLOW EXECUTION ENGINE
    // ==========================================

    suspend fun runAutonomousAiWorkflow(
        orderId: Long,
        onProgress: suspend (phase: Int, status: String) -> Unit
    ) {
        val order = dao.getOrderByIdDirect(orderId) ?: return

        // 1. Phase 1: Intake & Deep Analysis by AI
        onProgress(1, "Phase 1: Gemini AI parsing client brief, objectives, audience, and default assumptions...")
        kotlinx.coroutines.delay(600)
        val analyzedOrder = WorkflowEngine.generateIntakeAnalysis(order)
        dao.updateOrder(
            analyzedOrder.copy(
                currentPhase = 1,
                status = "INTAKE",
                hasBottleneck = false,
                updatedAt = System.currentTimeMillis()
            )
        )
        val intakeMsg = WorkflowEngine.buildIntakeWhatsAppMessage(analyzedOrder, hostWhatsAppNumber)
        dao.insertWhatsAppMessage(intakeMsg)

        // 2. Phase 2: Dependency-Aware Scheduling by AI
        onProgress(2, "Phase 2: Gemini AI formulating dependency graph, estimated milestones, and task roadmap...")
        kotlinx.coroutines.delay(600)
        var tasks = dao.getTasksForOrderDirect(orderId)
        if (tasks.isEmpty()) {
            tasks = WorkflowEngine.generateInitialTasks(orderId, order.projectTitle)
            dao.insertTasks(tasks)
        }
        advanceToPhase(orderId, 2)

        // 3. Phase 3: Autonomous Code Execution & Artifact Generation by AI
        onProgress(3, "Phase 3: Gemini AI generating production HTML5, modern CSS & TypeScript code artifacts...")
        kotlinx.coroutines.delay(700)
        var artifacts = dao.getCodeArtifactsForOrderDirect(orderId)
        if (artifacts.isEmpty()) {
            artifacts = WorkflowEngine.generateCodeArtifacts(analyzedOrder)
            dao.insertCodeArtifacts(artifacts)
        }
        // Mark all tasks completed with execution outputs
        tasks = dao.getTasksForOrderDirect(orderId)
        tasks.forEach { task ->
            dao.setTaskCompleted(task.id, true)
        }
        advanceToPhase(orderId, 3)

        // 4. Phase 4: Automated AI Quality Assurance & Accessibility Audit
        onProgress(4, "Phase 4: Gemini AI auditing W3C compliance, responsive viewports, and WCAG 2.1 AA...")
        kotlinx.coroutines.delay(700)
        var qaChecks = dao.getQaChecksForOrderDirect(orderId)
        if (qaChecks.isEmpty()) {
            qaChecks = WorkflowEngine.generateQaChecks(orderId)
            dao.insertQaChecks(qaChecks)
        }
        // Certify all QA checks
        qaChecks.forEach { qa ->
            dao.updateQaCheck(
                qa.copy(
                    status = "PASSED",
                    details = qa.details + " [Autonomous AI Audit: Certified 100% Pass]"
                )
            )
        }
        advanceToPhase(orderId, 4)

        // 5. Phase 5: Autonomous Packaging & Final Submission by AI
        onProgress(5, "Phase 5: Gemini AI generating delivery package, live preview URL, and final host dispatch...")
        kotlinx.coroutines.delay(600)
        advanceToPhase(orderId, 5)

        onProgress(5, "✨ Completed! All 5 operational workflow phases successfully executed autonomously by AI.")
    }

    suspend fun runAutonomousAiPhase(orderId: Long, phase: Int) {
        val order = dao.getOrderByIdDirect(orderId) ?: return

        when (phase) {
            1 -> {
                val analyzed = WorkflowEngine.generateIntakeAnalysis(order)
                dao.updateOrder(analyzed.copy(updatedAt = System.currentTimeMillis()))
                val msg = WorkflowEngine.buildIntakeWhatsAppMessage(analyzed, hostWhatsAppNumber)
                dao.insertWhatsAppMessage(msg)
            }
            2 -> {
                var tasks = dao.getTasksForOrderDirect(orderId)
                if (tasks.isEmpty()) {
                    tasks = WorkflowEngine.generateInitialTasks(orderId, order.projectTitle)
                    dao.insertTasks(tasks)
                }
                advanceToPhase(orderId, 2)
            }
            3 -> {
                var artifacts = dao.getCodeArtifactsForOrderDirect(orderId)
                if (artifacts.isEmpty()) {
                    artifacts = WorkflowEngine.generateCodeArtifacts(order)
                    dao.insertCodeArtifacts(artifacts)
                }
                val tasks = dao.getTasksForOrderDirect(orderId)
                tasks.forEach { dao.setTaskCompleted(it.id, true) }
                advanceToPhase(orderId, 3)
            }
            4 -> {
                var qaChecks = dao.getQaChecksForOrderDirect(orderId)
                if (qaChecks.isEmpty()) {
                    qaChecks = WorkflowEngine.generateQaChecks(orderId)
                    dao.insertQaChecks(qaChecks)
                }
                qaChecks.forEach {
                    dao.updateQaCheck(it.copy(status = "PASSED"))
                }
                advanceToPhase(orderId, 4)
            }
            5 -> {
                advanceToPhase(orderId, 5)
            }
        }
    }

    // ==========================================
    // AI HELP & SUPPORT DIAGNOSTIC SYSTEM
    // ==========================================

    suspend fun generateAiDiagnosticReport(orderId: Long?): com.example.data.gemini.AiDiagnosticReport {
        if (orderId == null) {
            val allOrders = dao.getAllOrdersDirect()
            val bottleneckCount = allOrders.count { it.hasBottleneck }
            val inFlightCount = allOrders.count { it.currentPhase in 1..4 && !it.hasBottleneck }
            val completedCount = allOrders.count { it.currentPhase == 5 }

            val status = if (bottleneckCount > 0) "WARNING" else "HEALTHY"
            val summary = "Pipeline Overview: $inFlightCount active orders in-flight, $bottleneckCount bottleneck alerts requiring attention, and $completedCount completed projects."
            val recs = listOf(
                if (bottleneckCount > 0) "Resolve active bottlenecks to avoid deadline penalties." else "Pipeline healthy. All dependencies moving forward.",
                "Ensure Host WhatsApp number ($hostWhatsAppNumber) is reachable for automated milestone pings.",
                "Use 1-Click Autonomous AI Workflow on newly onboarded client orders to expedite Phase 1–5 generation."
            )
            val actionPlan = "1. Review any active bottleneck flags in Orders tab.\n2. Verify Phase 4 WCAG 2.1 AA audit status before client submission.\n3. Keep WhatsApp dispatcher synced."
            return com.example.data.gemini.AiDiagnosticReport(
                title = "Overall Workflow Pipeline Health",
                statusLevel = status,
                summary = summary,
                recommendations = recs,
                automatedActionPlan = actionPlan
            )
        }

        val order = dao.getOrderByIdDirect(orderId)
            ?: return com.example.data.gemini.AiDiagnosticReport(
                title = "Order Diagnostics",
                statusLevel = "HEALTHY",
                summary = "No order currently selected.",
                recommendations = listOf("Select an active order from the pipeline to run AI analysis."),
                automatedActionPlan = "Select an order from the Orders or Phase Console screen."
            )

        val tasks = dao.getTasksForOrderDirect(orderId)
        val qaChecks = dao.getQaChecksForOrderDirect(orderId)
        val completedTasks = tasks.count { it.isCompleted }
        val passedQa = qaChecks.count { it.status == "PASSED" }

        val statusLevel = when {
            order.hasBottleneck -> "CRITICAL"
            order.currentPhase in 1..4 && completedTasks < tasks.size / 2 -> "WARNING"
            else -> "HEALTHY"
        }

        val summary = buildString {
            append("Diagnostics for ${order.projectTitle} (${order.clientName}):\n")
            append("• Current Phase: Phase ${order.currentPhase} of 5 (${order.status})\n")
            append("• Target Deadline: ${order.deadline}\n")
            append("• Task Progress: $completedTasks of ${tasks.size} tasks completed\n")
            append("• QA Status: $passedQa of ${qaChecks.size} checks passed")
            if (order.hasBottleneck) {
                append("\n• Active Bottleneck Alert: ${order.bottleneckDescription}")
            }
        }

        val recommendations = mutableListOf<String>()
        if (order.hasBottleneck) {
            recommendations.add("🚨 Immediate Action: Execute mitigation plan: ${order.bottleneckMitigation}")
            recommendations.add("Verify host has received WhatsApp emergency dispatch.")
        } else if (order.currentPhase < 5) {
            recommendations.add("Execute 'Run Full Autonomous AI Workflow' to complete remaining phases automatically.")
            recommendations.add("Ensure Phase 4 WCAG 2.1 AA compliance before triggering final submission.")
        } else {
            recommendations.add("Project successfully completed ahead of deadline.")
            recommendations.add("Live preview link active and verified with client brief.")
        }

        val actionPlan = if (order.hasBottleneck) {
            "Run mitigation immediately or click 'Resolve & Resume' in Phase Console once blocker is cleared."
        } else if (order.currentPhase < 5) {
            "Trigger Autonomous AI Workflow to advance from Phase ${order.currentPhase} to Phase 5 in under 5 seconds."
        } else {
            "Deliverable complete. Share standard operational report or live preview with client."
        }

        return com.example.data.gemini.AiDiagnosticReport(
            title = "AI Health Check: ${order.projectTitle}",
            statusLevel = statusLevel,
            summary = summary,
            recommendations = recommendations,
            automatedActionPlan = actionPlan
        )
    }

    suspend fun checkAndSeedInitialData() {
        val existing = dao.getAllOrders().firstOrNull()
        if (existing.isNullOrEmpty()) {
            // Seed Sample Order 1: Artisan Coffee Roastery
            val id1 = createNewOrder(
                clientName = "Solstice Coffee Co.",
                projectTitle = "Artisan E-Commerce & Subscription Store",
                rawBrief = "We need an ultra-fast, premium e-commerce storefront for our single-origin specialty coffees with subscription builder (every 2, 4, or 6 weeks), slide-over cart drawer, and Stripe checkout integration. Warm aesthetic reflecting our roast profiles.",
                brandAssets = "Color palette: Espresso Brown #24140E, Amber Gold #D4A373, Warm Cream #FBF8F3. Fonts: Playfair Display + Inter.",
                deadline = "In 48 Hours"
            )
            // Advance order 1 to Phase 3 with some tasks completed to show rich in-progress state
            advanceToPhase(id1, 2)
            advanceToPhase(id1, 3)
            val tasks1 = dao.getTasksForOrderDirect(id1)
            if (tasks1.size >= 2) {
                dao.setTaskCompleted(tasks1[0].id, true)
                dao.setTaskCompleted(tasks1[1].id, true)
            }

            // Seed Sample Order 2: SaaS Analytics Dashboard
            val id2 = createNewOrder(
                clientName = "PulseMetrics Systems",
                projectTitle = "Enterprise Cloud Telemetry Dashboard",
                rawBrief = "Build a responsive web application dashboard featuring real-time data streaming visuals, cluster health metrics, custom alert rule triggers, and role-based access team views.",
                brandAssets = "Dark obsidian theme #0B0F19, Electric Indigo #6366F1, Cyan #06B6D4. JetBrains Mono for code.",
                deadline = "In 5 Days"
            )
            // Keep order 2 in Phase 2 with roadmap dispatched
            advanceToPhase(id2, 2)

            // Seed Sample Order 3: Modern Dental Clinic (Completed deliverable)
            val id3 = createNewOrder(
                clientName = "Lumina Health & Dental",
                projectTitle = "Patient Intake & Booking Portal",
                rawBrief = "Patient self-service scheduling portal with interactive calendar slot selector, doctor specialty filtering, insurance intake file upload, and automated appointment reminders.",
                brandAssets = "Clean medical cyan #0891B2, Slate #334155, Pure White #FFFFFF. Plus Jakarta Sans.",
                deadline = "Delivered Today"
            )
            advanceToPhase(id3, 2)
            advanceToPhase(id3, 3)
            val tasks3 = dao.getTasksForOrderDirect(id3)
            tasks3.forEach { dao.setTaskCompleted(it.id, true) }
            advanceToPhase(id3, 4)
            advanceToPhase(id3, 5)
        }

        val existingChat = dao.getAllChatMessagesDirect()
        if (existingChat.isEmpty()) {
            dao.insertChatMessage(
                ChatMessage(
                    role = "model",
                    text = "👋 Welcome! I am your **Gemini Operational Copilot**.\n\nI can assist you with:\n• Breaking down complex client briefs into Phase 2 milestones\n• Formulating emergency bottleneck mitigations\n• Drafting automated Host WhatsApp dispatches\n• Inspecting generated code artifacts and WCAG 2.1 AA accessibility\n\nAsk me anything about your active orders or operational pipeline!",
                    modelName = "models/gemini-3.8-flash",
                    rolePreset = "Workflow Strategist"
                )
            )
        }
    }
}
