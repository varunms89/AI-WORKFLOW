package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.data.model.WhatsAppMessage
import com.example.domain.WorkflowEngine
import com.example.ui.components.CodeBlockView
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WhatsAppGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StandardReportScreen(
    order: ClientOrder?,
    allOrders: List<ClientOrder>,
    tasks: List<ProjectTask>,
    artifacts: List<CodeArtifact>,
    messages: List<WhatsAppMessage>,
    onSelectOrder: (Long) -> Unit,
    onShareReport: (ClientOrder) -> Unit
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
                text = "Select an order to view its verbatim 5-section Standard Output Report.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedArtifactIndex by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("standard_report_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Order selector & Action bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Standard Output Format",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Structured 5-section deliverable report",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                val reportString = WorkflowEngine.buildStandardOutputReport(
                                    order = order,
                                    tasks = tasks,
                                    artifacts = artifacts,
                                    messages = messages
                                )
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Standard Report", reportString)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Full report copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Report")
                        }
                        IconButton(onClick = { onShareReport(order) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share Report")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Order selector chips
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
                            label = { Text(itemOrder.clientName) },
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
            }
        }

        // Section 1: Brief Analysis & Scope Summary
        item {
            ReportCardSection(sectionNumber = "1", title = "Brief Analysis & Scope Summary") {
                ReportField(label = "Client Name", value = order.clientName)
                ReportField(label = "Project Title", value = order.projectTitle)
                ReportField(label = "Specified Deadline", value = order.deadline)
                ReportField(label = "Core Objectives", value = order.coreObjectives)
                ReportField(label = "Target Audience", value = order.targetAudience)
                ReportField(label = "Required Features", value = order.requiredFeatures)
                ReportField(label = "Design Preferences", value = order.designPreferences)
                ReportField(label = "Flagged Ambiguities & Default Assumptions", value = order.ambiguitiesAndAssumptions, isCode = false)
            }
        }

        // Section 2: Project Schedule & Task Breakdown
        item {
            ReportCardSection(sectionNumber = "2", title = "Project Schedule & Task Breakdown") {
                if (tasks.isEmpty()) {
                    Text("No tasks scheduled.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    tasks.forEachIndexed { idx, task ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${idx + 1}. ${task.title}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = if (task.isCompleted) "[X] COMPLETED" else "[ ] PENDING",
                                        color = if (task.isCompleted) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Category: ${task.category} • Est: ${task.estimatedDuration} • Milestone: ${task.milestoneTime}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Dependencies: ${task.dependencies} | Output: ${task.executionOutputSnippet}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Execution Log / Code Artifacts
        item {
            ReportCardSection(sectionNumber = "3", title = "Execution Log / Code Artifacts") {
                if (artifacts.isEmpty()) {
                    Text("No code artifacts generated.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
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

                    val current = artifacts.getOrNull(selectedArtifactIndex) ?: artifacts[0]
                    Text(
                        text = current.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CodeBlockView(
                        fileName = current.fileName,
                        language = current.language,
                        code = current.code
                    )
                }
            }
        }

        // Section 4: Host WhatsApp Dispatch Log (Preview of messages sent to the host's WhatsApp)
        item {
            ReportCardSection(
                sectionNumber = "4",
                title = "Host WhatsApp Dispatch Log",
                subtitle = "Preview of messages sent to the host's WhatsApp"
            ) {
                if (messages.isEmpty()) {
                    Text("No WhatsApp messages dispatched yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    messages.forEach { msg ->
                        val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(msg.timestamp))
                        Surface(
                            color = Color(0xFFF0FDF4),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "To: ${msg.recipientNumber} (${msg.messageType})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF065F46)
                                    )
                                    Text(
                                        text = timeStr,
                                        fontSize = 10.sp,
                                        color = Color(0xFF047857)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = msg.content,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF1E293B),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Final Delivery Confirmation
        item {
            ReportCardSection(sectionNumber = "5", title = "Final Delivery Confirmation") {
                ReportField(label = "Project Status", value = order.status)
                ReportField(
                    label = "Live Preview URL",
                    value = if (order.livePreviewUrl.isNotBlank()) order.livePreviewUrl else "https://preview.clientops.app/live/${order.id}"
                )
                ReportField(
                    label = "Deployment Instructions",
                    value = order.deploymentInstructions.ifBlank {
                        "Extract production artifact bundle. Run 'npm install && npm run build'. Deploy to Vercel/Cloudflare edge CDN with zero downtime."
                    }
                )
                ReportField(
                    label = "Final Submission State",
                    value = "All project files packaged, validated against client brief, and delivered to schedule."
                )
            }
        }

        // Bottom Spacing & Quick Copy Full Report Button
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        val reportString = WorkflowEngine.buildStandardOutputReport(
                            order = order,
                            tasks = tasks,
                            artifacts = artifacts,
                            messages = messages
                        )
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Standard Report", reportString)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Full report copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Full Standard Report")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ReportCardSection(
    sectionNumber: String,
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "SEC $sectionNumber",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

            content()
        }
    }
}

@Composable
fun ReportField(
    label: String,
    value: String,
    isCode: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = if (isCode) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 18.sp
        )
    }
}
