package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.gemini.GeminiRoleConfig
import com.example.data.gemini.GeminiRoles
import com.example.data.model.ChatMessage
import com.example.data.model.ClientOrder
import com.example.data.model.CodeArtifact
import com.example.data.model.ProjectTask
import com.example.data.model.QaCheck
import com.example.data.model.WhatsAppMessage
import com.example.data.repository.WorkflowRepository
import com.example.domain.WorkflowEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLEncoder

enum class NavigationTab(val title: String) {
    ORDERS("Orders"),
    PHASE_CONSOLE("Phase Console"),
    STANDARD_REPORT("Standard Report"),
    WHATSAPP_LOG("WhatsApp Log"),
    AI_SUPPORT("AI Support")
}

@OptIn(ExperimentalCoroutinesApi::class)
class WorkflowViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorkflowRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = WorkflowRepository(db.workflowDao(), application)
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    private val _currentTab = MutableStateFlow(NavigationTab.ORDERS)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _selectedOrderId = MutableStateFlow<Long?>(null)
    val selectedOrderId: StateFlow<Long?> = _selectedOrderId.asStateFlow()

    private val _hostWhatsAppNumber = MutableStateFlow(repository.hostWhatsAppNumber)
    val hostWhatsAppNumber: StateFlow<String> = _hostWhatsAppNumber.asStateFlow()

    private val _showNewOrderDialog = MutableStateFlow(false)
    val showNewOrderDialog: StateFlow<Boolean> = _showNewOrderDialog.asStateFlow()

    private val _showBottleneckDialog = MutableStateFlow(false)
    val showBottleneckDialog: StateFlow<Boolean> = _showBottleneckDialog.asStateFlow()

    private val _showHostSettingsDialog = MutableStateFlow(false)
    val showHostSettingsDialog: StateFlow<Boolean> = _showHostSettingsDialog.asStateFlow()

    // Autonomous AI Workflow Execution State
    private val _isAiWorkflowRunning = MutableStateFlow(false)
    val isAiWorkflowRunning: StateFlow<Boolean> = _isAiWorkflowRunning.asStateFlow()

    private val _aiWorkflowProgressPhase = MutableStateFlow(1)
    val aiWorkflowProgressPhase: StateFlow<Int> = _aiWorkflowProgressPhase.asStateFlow()

    private val _aiWorkflowProgressText = MutableStateFlow("")
    val aiWorkflowProgressText: StateFlow<String> = _aiWorkflowProgressText.asStateFlow()

    // AI Diagnostic & Support State
    private val _aiDiagnosticReport = MutableStateFlow<com.example.data.gemini.AiDiagnosticReport?>(null)
    val aiDiagnosticReport: StateFlow<com.example.data.gemini.AiDiagnosticReport?> = _aiDiagnosticReport.asStateFlow()

    private val _isDiagnosing = MutableStateFlow(false)
    val isDiagnosing: StateFlow<Boolean> = _isDiagnosing.asStateFlow()

    // Gemini Chat State
    val chatMessages: StateFlow<List<ChatMessage>> = repository.getAllChatMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedModel = MutableStateFlow("models/gemini-3.8-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _selectedRole = MutableStateFlow(GeminiRoles.WORKFLOW_STRATEGIST)
    val selectedRole: StateFlow<GeminiRoleConfig> = _selectedRole.asStateFlow()

    private val _isGeneratingResponse = MutableStateFlow(false)
    val isGeneratingResponse: StateFlow<Boolean> = _isGeneratingResponse.asStateFlow()

    private val _isChatExpanded = MutableStateFlow(false)
    val isChatExpanded: StateFlow<Boolean> = _isChatExpanded.asStateFlow()

    // All orders
    val orders: StateFlow<List<ClientOrder>> = repository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected order
    val selectedOrder: StateFlow<ClientOrder?> = _selectedOrderId.flatMapLatest { id ->
        if (id != null) repository.getOrderById(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Tasks for selected order
    val tasks: StateFlow<List<ProjectTask>> = _selectedOrderId.flatMapLatest { id ->
        if (id != null) repository.getTasksForOrder(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Code artifacts for selected order
    val artifacts: StateFlow<List<CodeArtifact>> = _selectedOrderId.flatMapLatest { id ->
        if (id != null) repository.getCodeArtifactsForOrder(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // QA checks for selected order
    val qaChecks: StateFlow<List<QaCheck>> = _selectedOrderId.flatMapLatest { id ->
        if (id != null) repository.getQaChecksForOrder(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // WhatsApp messages (all)
    val allWhatsAppMessages: StateFlow<List<WhatsAppMessage>> = repository.getAllWhatsAppMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // WhatsApp messages for selected order
    val orderWhatsAppMessages: StateFlow<List<WhatsAppMessage>> = _selectedOrderId.flatMapLatest { id ->
        if (id != null) repository.getWhatsAppMessagesForOrder(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun selectOrder(orderId: Long) {
        _selectedOrderId.value = orderId
    }

    fun selectOrderAndOpenConsole(orderId: Long) {
        _selectedOrderId.value = orderId
        _currentTab.value = NavigationTab.PHASE_CONSOLE
    }

    fun selectOrderAndOpenReport(orderId: Long) {
        _selectedOrderId.value = orderId
        _currentTab.value = NavigationTab.STANDARD_REPORT
    }

    fun setShowNewOrderDialog(show: Boolean) {
        _showNewOrderDialog.value = show
    }

    fun setShowBottleneckDialog(show: Boolean) {
        _showBottleneckDialog.value = show
    }

    fun setShowHostSettingsDialog(show: Boolean) {
        _showHostSettingsDialog.value = show
    }

    fun updateHostNumber(newNumber: String) {
        repository.hostWhatsAppNumber = newNumber
        _hostWhatsAppNumber.value = newNumber
    }

    fun createOrder(
        clientName: String,
        projectTitle: String,
        rawBrief: String,
        brandAssets: String,
        deadline: String,
        autoRunAiWorkflow: Boolean = true
    ) {
        viewModelScope.launch {
            val newId = repository.createNewOrder(
                clientName = clientName.trim(),
                projectTitle = projectTitle.trim(),
                rawBrief = rawBrief.trim(),
                brandAssets = brandAssets.trim(),
                deadline = deadline.trim()
            )
            _selectedOrderId.value = newId
            _showNewOrderDialog.value = false
            _currentTab.value = NavigationTab.PHASE_CONSOLE

            if (autoRunAiWorkflow) {
                runFullAutonomousAiWorkflow(newId)
            }
        }
    }

    // Run 5-Phase Full Autonomous AI Pipeline
    fun runFullAutonomousAiWorkflow(orderId: Long) {
        if (_isAiWorkflowRunning.value) return
        viewModelScope.launch {
            _isAiWorkflowRunning.value = true
            try {
                repository.runAutonomousAiWorkflow(orderId) { phase, statusText ->
                    _aiWorkflowProgressPhase.value = phase
                    _aiWorkflowProgressText.value = statusText
                }
            } finally {
                _isAiWorkflowRunning.value = false
            }
        }
    }

    fun runAutonomousAiWorkflow(orderId: Long) = runFullAutonomousAiWorkflow(orderId)

    // Run AI on single phase
    fun runAutonomousAiPhase(orderId: Long, phase: Int) {
        viewModelScope.launch {
            repository.runAutonomousAiPhase(orderId, phase)
        }
    }

    // Run AI Diagnostics on selected or overall pipeline
    fun runAiDiagnostics(orderId: Long? = null) {
        viewModelScope.launch {
            _isDiagnosing.value = true
            try {
                val targetId = orderId ?: _selectedOrderId.value
                val report = repository.generateAiDiagnosticReport(targetId)
                _aiDiagnosticReport.value = report
            } finally {
                _isDiagnosing.value = false
            }
        }
    }

    fun advanceOrderPhase(orderId: Long, targetPhase: Int) {
        viewModelScope.launch {
            repository.advanceToPhase(orderId, targetPhase)
        }
    }

    fun completeTask(taskId: Long, orderId: Long) {
        viewModelScope.launch {
            repository.markTaskCompleted(taskId, orderId)
        }
    }

    fun reportBottleneck(orderId: Long, bottleneck: String, mitigation: String) {
        viewModelScope.launch {
            repository.reportBottleneck(orderId, bottleneck, mitigation)
            _showBottleneckDialog.value = false
        }
    }

    fun resolveBottleneck(orderId: Long) {
        viewModelScope.launch {
            repository.resolveBottleneck(orderId)
        }
    }

    fun getStandardOutputReportString(order: ClientOrder): String {
        return WorkflowEngine.buildStandardOutputReport(
            order = order,
            tasks = tasks.value,
            artifacts = artifacts.value,
            messages = orderWhatsAppMessages.value
        )
    }

    fun launchWhatsAppMessage(context: Context, message: WhatsAppMessage) {
        try {
            val rawPhone = message.recipientNumber.replace(Regex("[^0-9+]"), "")
            val encodedText = URLEncoder.encode(message.content, "UTF-8")
            val url = "https://api.whatsapp.com/send?phone=$rawPhone&text=$encodedText"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to generic share intent if WhatsApp is not installed
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Host WhatsApp: ${message.messageType}")
                putExtra(Intent.EXTRA_TEXT, message.content)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(shareIntent, "Dispatch message via"))
            Toast.makeText(context, "Opening dispatcher...", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareFullReport(context: Context, order: ClientOrder) {
        val report = getStandardOutputReportString(order)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Standard Operational Report - ${order.projectTitle}")
            putExtra(Intent.EXTRA_TEXT, report)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Standard Output Report"))
    }

    // Gemini Chat Actions
    fun sendChatMessage(text: String) {
        if (text.isBlank() || _isGeneratingResponse.value) return
        val currentModel = _selectedModel.value
        val currentRole = _selectedRole.value
        val currentActiveOrder = selectedOrder.value

        viewModelScope.launch {
            _isGeneratingResponse.value = true
            try {
                repository.sendChatMessage(
                    userText = text.trim(),
                    modelName = currentModel,
                    roleConfig = currentRole,
                    activeOrder = currentActiveOrder
                )
            } finally {
                _isGeneratingResponse.value = false
            }
        }
    }

    fun setSelectedModel(model: String) {
        _selectedModel.value = model
    }

    fun setSelectedRole(role: GeminiRoleConfig) {
        _selectedRole.value = role
        // Align model if requested
        if (role.recommendedModel.isNotBlank()) {
            _selectedModel.value = role.recommendedModel
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    fun setChatExpanded(expanded: Boolean) {
        _isChatExpanded.value = expanded
    }
}

