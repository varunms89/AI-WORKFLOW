package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.NavigationTab
import com.example.ui.WorkflowViewModel
import com.example.ui.chat.GeminiFullScreenChatSheet
import com.example.ui.screens.AiSupportScreen
import com.example.ui.screens.BottleneckDialog
import com.example.ui.screens.HostSettingsDialog
import com.example.ui.screens.NewOrderDialog
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.PhaseConsoleScreen
import com.example.ui.screens.StandardReportScreen
import com.example.ui.screens.WhatsAppLogScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WhatsAppGreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: WorkflowViewModel = viewModel()) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val selectedOrderId by viewModel.selectedOrderId.collectAsStateWithLifecycle()
    val selectedOrder by viewModel.selectedOrder.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val artifacts by viewModel.artifacts.collectAsStateWithLifecycle()
    val qaChecks by viewModel.qaChecks.collectAsStateWithLifecycle()
    val allMessages by viewModel.allWhatsAppMessages.collectAsStateWithLifecycle()
    val orderMessages by viewModel.orderWhatsAppMessages.collectAsStateWithLifecycle()
    val hostNumber by viewModel.hostWhatsAppNumber.collectAsStateWithLifecycle()

    val showNewOrderDialog by viewModel.showNewOrderDialog.collectAsStateWithLifecycle()
    val showBottleneckDialog by viewModel.showBottleneckDialog.collectAsStateWithLifecycle()
    val showHostSettingsDialog by viewModel.showHostSettingsDialog.collectAsStateWithLifecycle()

    // Gemini Chat State
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val selectedRole by viewModel.selectedRole.collectAsStateWithLifecycle()
    val isGeneratingChat by viewModel.isGeneratingResponse.collectAsStateWithLifecycle()
    val isChatExpanded by viewModel.isChatExpanded.collectAsStateWithLifecycle()
    var showFullScreenChat by remember { mutableStateOf(false) }

    // Autonomous AI Workflow & Support State
    val isAiWorkflowRunning by viewModel.isAiWorkflowRunning.collectAsStateWithLifecycle()
    val aiWorkflowProgressPhase by viewModel.aiWorkflowProgressPhase.collectAsStateWithLifecycle()
    val aiWorkflowProgressText by viewModel.aiWorkflowProgressText.collectAsStateWithLifecycle()
    val aiDiagnosticReport by viewModel.aiDiagnosticReport.collectAsStateWithLifecycle()
    val isDiagnosing by viewModel.isDiagnosing.collectAsStateWithLifecycle()

    // Auto-select first order if none selected
    LaunchedEffect(orders, selectedOrderId) {
        if (selectedOrderId == null && orders.isNotEmpty()) {
            viewModel.selectOrder(orders.first().id)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Timeline,
                                    contentDescription = "App Logo",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Order Workflow",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Operational Phases & WhatsApp Dispatch",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Gemini AI Copilot quick button
                    IconButton(
                        onClick = { showFullScreenChat = true },
                        modifier = Modifier.testTag("top_bar_gemini_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini Copilot",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Host WhatsApp indicator chip
                    Surface(
                        color = WhatsAppGreen.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clickable { viewModel.setShowHostSettingsDialog(true) }
                            .padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(WhatsAppGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Host: $hostNumber",
                                color = WhatsAppGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == NavigationTab.ORDERS,
                    onClick = { viewModel.setTab(NavigationTab.ORDERS) },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Orders") },
                    label = { Text("Orders", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_orders")
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.PHASE_CONSOLE,
                    onClick = { viewModel.setTab(NavigationTab.PHASE_CONSOLE) },
                    icon = { Icon(Icons.Default.Timeline, contentDescription = "Console") },
                    label = { Text("Phases", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_console")
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.AI_SUPPORT,
                    onClick = { viewModel.setTab(NavigationTab.AI_SUPPORT) },
                    icon = {
                        if (isAiWorkflowRunning) {
                            BadgedBox(badge = { Badge { Text("AI", fontSize = 8.sp) } }) {
                                Icon(Icons.Default.SupportAgent, contentDescription = "AI Support")
                            }
                        } else {
                            Icon(Icons.Default.SupportAgent, contentDescription = "AI Support")
                        }
                    },
                    label = { Text("AI Support", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_ai_support")
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.STANDARD_REPORT,
                    onClick = { viewModel.setTab(NavigationTab.STANDARD_REPORT) },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Report") },
                    label = { Text("Report", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_report")
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.WHATSAPP_LOG,
                    onClick = { viewModel.setTab(NavigationTab.WHATSAPP_LOG) },
                    icon = { Icon(Icons.Default.Phone, contentDescription = "WhatsApp") },
                    label = { Text("WhatsApp", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_whatsapp")
                )
            }
        },
        floatingActionButton = {
            if (currentTab == NavigationTab.ORDERS) {
                FloatingActionButton(
                    onClick = { viewModel.setShowNewOrderDialog(true) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_new_order")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Order")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.ORDERS -> {
                    OrdersScreen(
                        orders = orders,
                        chatMessages = chatMessages,
                        selectedModel = selectedModel,
                        selectedRole = selectedRole,
                        isGeneratingChat = isGeneratingChat,
                        isChatExpanded = isChatExpanded,
                        onSendMessage = { text -> viewModel.sendChatMessage(text) },
                        onSelectRole = { role -> viewModel.setSelectedRole(role) },
                        onSelectModel = { model -> viewModel.setSelectedModel(model) },
                        onClearChat = { viewModel.clearChatHistory() },
                        onToggleExpandChat = { exp -> viewModel.setChatExpanded(exp) },
                        onOpenFullScreenChat = { showFullScreenChat = true },
                        onSelectOrderConsole = { id -> viewModel.selectOrderAndOpenConsole(id) },
                        onSelectOrderReport = { id -> viewModel.selectOrderAndOpenReport(id) },
                        onNewOrderClick = { viewModel.setShowNewOrderDialog(true) },
                        onRunAutonomousWorkflow = { id -> viewModel.runAutonomousAiWorkflow(id) },
                        onResolveBottleneck = { id -> viewModel.resolveBottleneck(id) }
                    )
                }
                NavigationTab.PHASE_CONSOLE -> {
                    PhaseConsoleScreen(
                        order = selectedOrder,
                        allOrders = orders,
                        tasks = tasks,
                        artifacts = artifacts,
                        qaChecks = qaChecks,
                        hostNumber = hostNumber,
                        isAiRunning = isAiWorkflowRunning,
                        aiProgressPhase = aiWorkflowProgressPhase,
                        aiProgressText = aiWorkflowProgressText,
                        onSelectOrder = { id -> viewModel.selectOrder(id) },
                        onAdvancePhase = { orderId, phase -> viewModel.advanceOrderPhase(orderId, phase) },
                        onCompleteTask = { taskId, orderId -> viewModel.completeTask(taskId, orderId) },
                        onReportBottleneckClick = { viewModel.setShowBottleneckDialog(true) },
                        onResolveBottleneck = { orderId -> viewModel.resolveBottleneck(orderId) },
                        onOpenWhatsAppLog = { viewModel.setTab(NavigationTab.WHATSAPP_LOG) },
                        onOpenStandardReport = { viewModel.setTab(NavigationTab.STANDARD_REPORT) },
                        onRunAutonomousWorkflow = { id -> viewModel.runAutonomousAiWorkflow(id) },
                        onRunAutonomousPhase = { id, phase -> viewModel.runAutonomousAiPhase(id, phase) }
                    )
                }
                NavigationTab.AI_SUPPORT -> {
                    AiSupportScreen(
                        orders = orders,
                        selectedOrder = selectedOrder,
                        chatMessages = chatMessages,
                        isGenerating = isGeneratingChat,
                        diagnosticReport = aiDiagnosticReport,
                        isDiagnosing = isDiagnosing,
                        onSelectOrder = { id -> viewModel.selectOrder(id) },
                        onRunDiagnostics = { id -> viewModel.runAiDiagnostics(id) },
                        onSendMessage = { text -> viewModel.sendChatMessage(text) },
                        onClearChat = { viewModel.clearChatHistory() },
                        onRunAutonomousWorkflow = { id -> viewModel.runAutonomousAiWorkflow(id) }
                    )
                }
                NavigationTab.STANDARD_REPORT -> {
                    StandardReportScreen(
                        order = selectedOrder,
                        allOrders = orders,
                        tasks = tasks,
                        artifacts = artifacts,
                        messages = if (selectedOrder != null) orderMessages else allMessages,
                        onSelectOrder = { id -> viewModel.selectOrder(id) },
                        onShareReport = { ord -> viewModel.shareFullReport(context, ord) }
                    )
                }
                NavigationTab.WHATSAPP_LOG -> {
                    WhatsAppLogScreen(
                        messages = allMessages,
                        hostNumber = hostNumber,
                        onEditHostNumberClick = { viewModel.setShowHostSettingsDialog(true) },
                        onLaunchWhatsApp = { msg -> viewModel.launchWhatsAppMessage(context, msg) }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showNewOrderDialog) {
        NewOrderDialog(
            onDismiss = { viewModel.setShowNewOrderDialog(false) },
            onSubmit = { client, title, brief, assets, deadline, autoRunAiWorkflow ->
                viewModel.createOrder(client, title, brief, assets, deadline, autoRunAiWorkflow)
            }
        )
    }

    if (showBottleneckDialog && selectedOrder != null) {
        BottleneckDialog(
            projectTitle = selectedOrder?.projectTitle ?: "",
            onDismiss = { viewModel.setShowBottleneckDialog(false) },
            onSubmit = { bottleneck, mitigation ->
                selectedOrder?.id?.let { viewModel.reportBottleneck(it, bottleneck, mitigation) }
            }
        )
    }

    if (showHostSettingsDialog) {
        HostSettingsDialog(
            currentNumber = hostNumber,
            onDismiss = { viewModel.setShowHostSettingsDialog(false) },
            onSave = { newNumber ->
                viewModel.updateHostNumber(newNumber)
                viewModel.setShowHostSettingsDialog(false)
            }
        )
    }

    if (showFullScreenChat) {
        GeminiFullScreenChatSheet(
            messages = chatMessages,
            selectedModel = selectedModel,
            selectedRole = selectedRole,
            isGenerating = isGeneratingChat,
            onDismiss = { showFullScreenChat = false },
            onSendMessage = { text -> viewModel.sendChatMessage(text) },
            onSelectRole = { role -> viewModel.setSelectedRole(role) },
            onSelectModel = { model -> viewModel.setSelectedModel(model) },
            onClearChat = { viewModel.clearChatHistory() }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

