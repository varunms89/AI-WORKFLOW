package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gemini.GeminiRoleConfig
import com.example.data.model.ChatMessage
import com.example.data.model.ClientOrder
import com.example.ui.chat.GeminiHomeScreenChatSection
import com.example.ui.components.AnimatedBottleneckAlertCard
import com.example.ui.components.PhaseBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WhatsAppGreen

@Composable
fun OrdersScreen(
    orders: List<ClientOrder>,
    chatMessages: List<ChatMessage>,
    selectedModel: String,
    selectedRole: GeminiRoleConfig,
    isGeneratingChat: Boolean,
    isChatExpanded: Boolean,
    onSendMessage: (String) -> Unit,
    onSelectRole: (GeminiRoleConfig) -> Unit,
    onSelectModel: (String) -> Unit,
    onClearChat: () -> Unit,
    onToggleExpandChat: (Boolean) -> Unit,
    onOpenFullScreenChat: () -> Unit,
    onSelectOrderConsole: (orderId: Long) -> Unit,
    onSelectOrderReport: (orderId: Long) -> Unit,
    onNewOrderClick: () -> Unit,
    onRunAutonomousWorkflow: (orderId: Long) -> Unit = {},
    onResolveBottleneck: (orderId: Long) -> Unit = {}
) {
    val totalOrders = orders.size
    val inFlight = orders.count { it.currentPhase in 1..4 && !it.hasBottleneck }
    val inBottleneck = orders.count { it.hasBottleneck }
    val completed = orders.count { it.currentPhase == 5 }
    val activeOrder = orders.firstOrNull { it.currentPhase in 1..4 } ?: orders.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("orders_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Overview Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Orders",
                    count = totalOrders.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "In-Flight",
                    count = inFlight.toString(),
                    color = Color(0xFF0284C7)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Bottlenecks",
                    count = inBottleneck.toString(),
                    color = if (inBottleneck > 0) AlertRed else Color(0xFF64748B)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Delivered",
                    count = completed.toString(),
                    color = SuccessGreen
                )
            }
        }

        // Gemini AI Operations Copilot Section (Home Screen Interactive Multi-turn Chat)
        item {
            GeminiHomeScreenChatSection(
                messages = chatMessages,
                selectedModel = selectedModel,
                selectedRole = selectedRole,
                isGenerating = isGeneratingChat,
                isExpanded = isChatExpanded,
                activeOrderTitle = activeOrder?.projectTitle,
                onSendMessage = onSendMessage,
                onSelectRole = onSelectRole,
                onSelectModel = onSelectModel,
                onClearChat = onClearChat,
                onToggleExpand = onToggleExpandChat,
                onOpenFullScreen = onOpenFullScreenChat
            )
        }


        // Section Title with + New Order Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Active Client Orders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sequential 5-Phase Automated Operational Pipeline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onNewOrderClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("new_order_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Order",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Order", fontSize = 13.sp)
                }
            }
        }

        // Empty state
        if (orders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Active Client Orders",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Intake a new client order to trigger Phase 1 Analysis, task breakdown, and automated Host WhatsApp dispatches.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNewOrderClick) {
                            Text("Execute New Client Order")
                        }
                    }
                }
            }
        } else {
            items(orders, key = { it.id }) { order ->
                OrderCard(
                    order = order,
                    onOpenConsole = { onSelectOrderConsole(order.id) },
                    onOpenReport = { onSelectOrderReport(order.id) },
                    onRunAutonomousWorkflow = { onRunAutonomousWorkflow(order.id) },
                    onResolveBottleneck = { onResolveBottleneck(order.id) }
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun OrderCard(
    order: ClientOrder,
    onOpenConsole: () -> Unit,
    onOpenReport: () -> Unit,
    onRunAutonomousWorkflow: () -> Unit,
    onResolveBottleneck: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenConsole() }
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Client Name + Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.clientName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.projectTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                PhaseBadge(phase = order.currentPhase)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Deadline & Scope Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Deadline",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Deadline: ${order.deadline}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusBadge(status = order.status)
            }

            // Animated Bottleneck Alert Banner if triggered
            if (order.hasBottleneck) {
                Spacer(modifier = Modifier.height(10.dp))
                AnimatedBottleneckAlertCard(
                    order = order,
                    onResolveBottleneck = { onResolveBottleneck() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Operational Phase Progress Bar (1..5)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Phase ${order.currentPhase} of 5",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(order.currentPhase * 20)}% Complete",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { order.currentPhase / 5f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (order.currentPhase == 5) SuccessGreen else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenConsole,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = "Console",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Phase Console", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenReport,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Report",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Standard Report", fontSize = 12.sp)
                }
            }

            // AI Autonomous Execution Trigger Button
            if (order.currentPhase < 5) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRunAutonomousWorkflow,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("order_auto_ai_${order.id}")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto-Execute All Phases with AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = SuccessGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Workflow Fully Executed & Delivered by AI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    }
                }
            }
        }
    }
}
