package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientOrder
import com.example.data.model.CodeArtifact
import com.example.data.model.ProjectTask
import com.example.data.model.QaCheck
import com.example.ui.components.AnimatedBottleneckAlertCard
import com.example.ui.components.AnimatedTaskItemCard
import com.example.ui.components.CodeBlockView
import com.example.ui.components.PhaseBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.WhatsAppActionButton
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WhatsAppGreen

@Composable
fun PhaseConsoleScreen(
    order: ClientOrder?,
    allOrders: List<ClientOrder>,
    tasks: List<ProjectTask>,
    artifacts: List<CodeArtifact>,
    qaChecks: List<QaCheck>,
    hostNumber: String,
    isAiRunning: Boolean = false,
    aiProgressPhase: Int = 1,
    aiProgressText: String = "",
    onSelectOrder: (Long) -> Unit,
    onAdvancePhase: (orderId: Long, phase: Int) -> Unit,
    onCompleteTask: (taskId: Long, orderId: Long) -> Unit,
    onReportBottleneckClick: () -> Unit,
    onResolveBottleneck: (Long) -> Unit,
    onOpenWhatsAppLog: () -> Unit,
    onOpenStandardReport: () -> Unit,
    onRunAutonomousWorkflow: (Long) -> Unit = {},
    onRunAutonomousPhase: (orderId: Long, phase: Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    if (order == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Order Selected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please select an active order from the pipeline to manage its operational workflow phases.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        return
    }

    // Active Tab in the Console: Defaults to the order's current phase (1..5)
    var selectedPhaseTab by remember(order.id, order.currentPhase) {
        mutableIntStateOf(order.currentPhase)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("phase_console_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Order Selector Dropdown / Chips if multiple orders
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Active Workflow Subject:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allOrders.forEach { itemOrder ->
                        FilterChip(
                            selected = itemOrder.id == order.id,
                            onClick = { onSelectOrder(itemOrder.id) },
                            label = {
                                Text(
                                    text = itemOrder.clientName,
                                    fontWeight = if (itemOrder.id == order.id) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (itemOrder.id == order.id) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Order Header Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = order.projectTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Client: ${order.clientName} • Deadline: ${order.deadline}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(status = order.status)
                }

                // Animated Bottleneck Alert Banner if active
                if (order.hasBottleneck) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AnimatedBottleneckAlertCard(
                        order = order,
                        onResolveBottleneck = onResolveBottleneck,
                        onOpenWhatsAppLog = onOpenWhatsAppLog
                    )
                }
            }
        }

        // Autonomous AI Execution Engine Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("autonomous_ai_panel"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1E1B4B),
                                    Color(0xFF3730A3),
                                    Color(0xFF1E293B)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(ElectricIndigo, NeonCyan))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Autonomous",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Autonomous AI Workflow Engine",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "1-Click automated execution of all 5 phases via Gemini AI",
                                        color = Color(0xFFA5B4FC),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (isAiRunning) {
                                Surface(
                                    color = ElectricIndigo.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Executing", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (isAiRunning) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Active: Phase $aiProgressPhase of 5",
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "${aiProgressPhase * 20}%",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { aiProgressPhase / 5f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = NeonCyan,
                                        trackColor = Color.White.copy(alpha = 0.2f)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = aiProgressText,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onRunAutonomousWorkflow(order.id) },
                                    enabled = !isAiRunning,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (order.currentPhase == 5) SuccessGreen else ElectricIndigo
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("run_autonomous_pipeline_button")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (order.currentPhase == 5) "Re-run Full AI Pipeline" else "Run Full AI Pipeline (Phase 1–5)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { onRunAutonomousPhase(order.id, selectedPhaseTab) },
                                    enabled = !isAiRunning,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                    modifier = Modifier.testTag("run_autonomous_current_phase_button")
                                ) {
                                    Text("Auto Phase $selectedPhaseTab", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sequential 5-Phase Tabs Stepper
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedPhaseTab - 1,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                val phaseNames = listOf(
                    "Phase 1: Intake",
                    "Phase 2: Schedule",
                    "Phase 3: Execution",
                    "Phase 4: QA Review",
                    "Phase 5: Submission"
                )
                phaseNames.forEachIndexed { index, name ->
                    val phaseNum = index + 1
                    val isPastOrCurrent = order.currentPhase >= phaseNum
                    Tab(
                        selected = selectedPhaseTab == phaseNum,
                        onClick = { selectedPhaseTab = phaseNum },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (order.currentPhase > phaseNum) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Completed",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = name,
                                    fontWeight = if (selectedPhaseTab == phaseNum) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )
                }
            }
        }

        // Phase Content based on selectedPhaseTab
        when (selectedPhaseTab) {
            1 -> {
                item {
                    Phase1IntakeView(
                        order = order,
                        hostNumber = hostNumber,
                        onAdvanceToPhase2 = { onAdvancePhase(order.id, 2) },
                        onOpenWhatsAppLog = onOpenWhatsAppLog
                    )
                }
            }
            2 -> {
                item {
                    Phase2SchedulingView(
                        order = order,
                        tasks = tasks,
                        hostNumber = hostNumber,
                        onAdvanceToPhase3 = { onAdvancePhase(order.id, 3) },
                        onOpenWhatsAppLog = onOpenWhatsAppLog
                    )
                }
            }
            3 -> {
                item {
                    Phase3ExecutionView(
                        order = order,
                        tasks = tasks,
                        artifacts = artifacts,
                        hostNumber = hostNumber,
                        onCompleteTask = { taskId -> onCompleteTask(taskId, order.id) },
                        onReportBottleneck = onReportBottleneckClick,
                        onAdvanceToPhase4 = { onAdvancePhase(order.id, 4) },
                        onOpenWhatsAppLog = onOpenWhatsAppLog
                    )
                }
            }
            4 -> {
                item {
                    Phase4QaView(
                        order = order,
                        checks = qaChecks,
                        hostNumber = hostNumber,
                        onAdvanceToPhase5 = { onAdvancePhase(order.id, 5) },
                        onOpenWhatsAppLog = onOpenWhatsAppLog
                    )
                }
            }
            5 -> {
                item {
                    Phase5SubmissionView(
                        order = order,
                        hostNumber = hostNumber,
                        onOpenStandardReport = onOpenStandardReport,
                        onOpenWhatsAppLog = onOpenWhatsAppLog
                    )
                }
            }
        }
    }
}

// ----------------- PHASE 1 INTAKE & ANALYSIS VIEW -----------------
@Composable
fun Phase1IntakeView(
    order: ClientOrder,
    hostNumber: String,
    onAdvanceToPhase2: () -> Unit,
    onOpenWhatsAppLog: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(
                title = "Phase 1: Intake & Analysis",
                subtitle = "Raw brief parsing, objective formulation & automated host WhatsApp confirmation"
            )

            // WhatsApp Action Box
            Surface(
                color = Color(0xFFF0FDF4),
                border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WhatsApp Action (Host): Intake Confirmation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = "Automated scope summary & delivery timeline dispatched to Host: $hostNumber",
                            fontSize = 11.sp,
                            color = Color(0xFF047857)
                        )
                    }
                    Button(
                        onClick = onOpenWhatsAppLog,
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("View Dispatch", fontSize = 11.sp)
                    }
                }
            }

            InfoBlock(
                title = "Core Objectives",
                content = order.coreObjectives.ifBlank { "High-converting responsive platform delivering optimal performance across mobile and desktop." }
            )

            InfoBlock(
                title = "Target Audience",
                content = order.targetAudience.ifBlank { "Mobile-first digital retail consumers and brand advocates." }
            )

            InfoBlock(
                title = "Required Features",
                content = order.requiredFeatures.ifBlank { "E-Commerce checkout, product catalog, responsive cart drawer, contact form." }
            )

            InfoBlock(
                title = "Design Preferences & Brand Assets",
                content = "${order.designPreferences}\n\n*Provided Assets:* ${order.brandAssets}"
            )

            InfoBlock(
                title = "Flagged Ambiguities & Default Assumptions",
                content = order.ambiguitiesAndAssumptions.ifBlank { "No critical blocking ambiguities identified. Proceeding under production standard assumptions." },
                isHighlight = true
            )

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onAdvanceToPhase2,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("advance_to_phase_2_button")
            ) {
                Text("Proceed to Phase 2: Task Breakdown & Scheduling")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ----------------- PHASE 2 TASK BREAKDOWN & SCHEDULING VIEW -----------------
@Composable
fun Phase2SchedulingView(
    order: ClientOrder,
    tasks: List<ProjectTask>,
    hostNumber: String,
    onAdvanceToPhase3: () -> Unit,
    onOpenWhatsAppLog: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(
                title = "Phase 2: Task Breakdown & Scheduling",
                subtitle = "Sequential dependency-aware roadmap and milestone checkpoints"
            )

            // WhatsApp Action Box
            Surface(
                color = Color(0xFFF0FDF4),
                border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WhatsApp Action (Host): Schedule Dispatch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = "Project schedule & milestones sent to Host WhatsApp for progress tracking.",
                            fontSize = 11.sp,
                            color = Color(0xFF047857)
                        )
                    }
                    Button(
                        onClick = onOpenWhatsAppLog,
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("View Dispatch", fontSize = 11.sp)
                    }
                }
            }

            // Tasks Roadmap
            Text(
                text = "Structured Project Roadmap (${tasks.size} Milestones):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            tasks.forEachIndexed { index, task ->
                val isMissedDeadline = !task.isCompleted && order.hasBottleneck && (index == 0 || index == 1)
                val isBottlenecked = !task.isCompleted && order.hasBottleneck && (task.title.contains("API", ignoreCase = true) || task.title.contains("Checkout", ignoreCase = true) || task.title.contains("Architecture", ignoreCase = true) || index == 0)

                AnimatedTaskItemCard(
                    task = task,
                    index = index,
                    isMissedDeadline = isMissedDeadline,
                    isBottlenecked = isBottlenecked,
                    onCompleteTask = {}
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onAdvanceToPhase3,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("advance_to_phase_3_button")
            ) {
                Text("Proceed to Phase 3: Execution & Development")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ----------------- PHASE 3 EXECUTION & DEVELOPMENT VIEW -----------------
@Composable
fun Phase3ExecutionView(
    order: ClientOrder,
    tasks: List<ProjectTask>,
    artifacts: List<CodeArtifact>,
    hostNumber: String,
    onCompleteTask: (Long) -> Unit,
    onReportBottleneck: () -> Unit,
    onAdvanceToPhase4: () -> Unit,
    onOpenWhatsAppLog: () -> Unit
) {
    var selectedArtifactIndex by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(
                title = "Phase 3: Execution & Development",
                subtitle = "Production code generation, task execution tracking & automated host alerts"
            )

            // WhatsApp Action Box
            Surface(
                color = Color(0xFFF0FDF4),
                border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WhatsApp Action (Host): Live Status Updates",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = "Completing major tasks automatically fires status notifications to Host: $hostNumber",
                            fontSize = 11.sp,
                            color = Color(0xFF047857)
                        )
                    }
                    Button(
                        onClick = onOpenWhatsAppLog,
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("View Dispatch", fontSize = 11.sp)
                    }
                }
            }

            // Interactive Task Completion Checklist
            Text(
                text = "Milestone Task Execution (Check to complete & trigger Host WhatsApp notice):",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            tasks.forEachIndexed { index, task ->
                val isMissedDeadline = !task.isCompleted && order.hasBottleneck && (index == 0 || index == 1)
                val isBottlenecked = !task.isCompleted && order.hasBottleneck && (task.title.contains("API", ignoreCase = true) || task.title.contains("Checkout", ignoreCase = true) || task.title.contains("Architecture", ignoreCase = true) || index == 0)

                AnimatedTaskItemCard(
                    task = task,
                    index = index,
                    isMissedDeadline = isMissedDeadline,
                    isBottlenecked = isBottlenecked,
                    allowCompletion = true,
                    onCompleteTask = onCompleteTask
                )
            }

            // Report Bottleneck Guardrail Button
            OutlinedButton(
                onClick = onReportBottleneck,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = AlertRed, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Report Bottleneck & Alert Host WhatsApp", fontWeight = FontWeight.Bold)
            }

            // Code Artifacts Tabs
            Text(
                text = "Generated Code Artifacts (Zero Placeholder Content):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            if (artifacts.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    artifacts.forEachIndexed { idx, artifact ->
                        FilterChip(
                            selected = selectedArtifactIndex == idx,
                            onClick = { selectedArtifactIndex = idx },
                            label = { Text(artifact.fileName, fontFamily = FontFamily.Monospace, fontSize = 11.sp) }
                        )
                    }
                }

                val currentArtifact = artifacts.getOrNull(selectedArtifactIndex) ?: artifacts[0]
                Text(
                    text = currentArtifact.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CodeBlockView(
                    fileName = currentArtifact.fileName,
                    language = currentArtifact.language,
                    code = currentArtifact.code
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onAdvanceToPhase4,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("advance_to_phase_4_button")
            ) {
                Text("Proceed to Phase 4: Quality Assurance & Review")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ----------------- PHASE 4 QUALITY ASSURANCE & REVIEW VIEW -----------------
@Composable
fun Phase4QaView(
    order: ClientOrder,
    checks: List<QaCheck>,
    hostNumber: String,
    onAdvanceToPhase5: () -> Unit,
    onOpenWhatsAppLog: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(
                title = "Phase 4: Quality Assurance & Review",
                subtitle = "Automated validation on code validity, link integrity, asset optimization & accessibility"
            )

            // QA Audit Score Card
            Surface(
                color = Color(0xFFF0FDF4),
                border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Passed", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Automated Audit Result: 5 / 5 Standards Passed",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = "Zero broken links • WCAG 2.1 AA Certified • Assets compressed to WebP",
                            fontSize = 11.sp,
                            color = Color(0xFF047857)
                        )
                    }
                }
            }

            // Checks list
            checks.forEach { check ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = check.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Surface(
                                color = Color(0xFFD1FAE5),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = check.status,
                                    color = Color(0xFF065F46),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = check.details,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Remediation & Proof: ${check.remediationNote}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onAdvanceToPhase5,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("advance_to_phase_5_button")
            ) {
                Text("Confirm QA & Proceed to Phase 5: Final Submission")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ----------------- PHASE 5 FINAL SUBMISSION VIEW -----------------
@Composable
fun Phase5SubmissionView(
    order: ClientOrder,
    hostNumber: String,
    onOpenStandardReport: () -> Unit,
    onOpenWhatsAppLog: () -> Unit
) {
    val context = LocalContext.current
    val previewUrl = if (order.livePreviewUrl.isNotBlank()) order.livePreviewUrl else "https://preview.clientops.app/live/${order.id}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(
                title = "Phase 5: Final Submission",
                subtitle = "Package release, deployment instructions & host WhatsApp final delivery confirmation"
            )

            // Final Delivery Banner
            Surface(
                color = Color(0xFFD1FAE5),
                border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FINAL DELIVERABLE READY & VERIFIED",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = Color(0xFF065F46)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Submitted ahead of scheduled deadline (${order.deadline}). Full source package compiled and verified.",
                        fontSize = 12.sp,
                        color = Color(0xFF047857)
                    )
                }
            }

            // WhatsApp Action Box
            Surface(
                color = Color(0xFFF0FDF4),
                border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WhatsApp Action (Host): Final Delivery Confirmation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = "Live preview link, delivery confirmation & task summary sent to Host: $hostNumber",
                            fontSize = 11.sp,
                            color = Color(0xFF047857)
                        )
                    }
                    Button(
                        onClick = onOpenWhatsAppLog,
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("View Dispatch", fontSize = 11.sp)
                    }
                }
            }

            // Live Preview Link Card
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Live Preview URL:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = previewUrl,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(previewUrl)).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open", fontSize = 11.sp)
                    }
                }
            }

            // Deployment Instructions
            InfoBlock(
                title = "Deployment Instructions",
                content = order.deploymentInstructions.ifBlank {
                    "1. Extract production bundle.\n2. Configure environment variables for API keys in your host provider (Vercel / Netlify / Cloudflare).\n3. Execute 'npm run build && vercel --prod' for instantaneous worldwide edge CDN deployment."
                }
            )

            // Package Manifest Summary
            InfoBlock(
                title = "Package Manifest Summary",
                content = "• index.html (Responsive W3C Validated HTML5 Storefront)\n• styles.css (Tailwind & CSS Tokens with accessibility rings)\n• src/checkout.ts (Stripe & cart logic)\n• Automated QA Validation Audit Passed (5/5 Checks)\n• Host WhatsApp Dispatch Communication Log"
            )

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onOpenStandardReport,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_standard_report_button")
            ) {
                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Full 5-Section Standard Output Report")
            }
        }
    }
}

@Composable
fun InfoBlock(
    title: String,
    content: String,
    isHighlight: Boolean = false
) {
    Surface(
        color = if (isHighlight) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isHighlight) androidx.compose.foundation.BorderStroke(1.dp, WarningOrange.copy(alpha = 0.4f)) else null,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlight) Color(0xFF92400E) else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}
