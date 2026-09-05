package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricIndigo

@Composable
fun NewOrderDialog(
    onDismiss: () -> Unit,
    onSubmit: (client: String, title: String, brief: String, assets: String, deadline: String, autoRunAi: Boolean) -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var projectTitle by remember { mutableStateOf("") }
    var rawBrief by remember { mutableStateOf("") }
    var brandAssets by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("In 48 Hours") }
    var autoRunAiWorkflow by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ElectricIndigo,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "New Client Order Intake",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Automated AI-driven project delivery pipeline",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Autonomous AI Workflow Switch
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ElectricIndigo.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Auto-Execute with Gemini AI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ElectricIndigo
                                )
                            }
                            Text(
                                text = "Autonomously parse brief, schedule tasks, generate code, run QA & prepare delivery.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoRunAiWorkflow,
                            onCheckedChange = { autoRunAiWorkflow = it },
                            modifier = Modifier.testTag("auto_run_ai_switch")
                        )
                    }
                }

                // Quick presets
                Text(
                    text = "Quick Presets:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            clientName = "Summit Peak Roasters"
                            projectTitle = "High-Altitude Coffee E-Commerce"
                            rawBrief = "Direct-to-consumer artisanal coffee subscription portal with roast quiz, cart drawer, and Stripe checkout."
                            brandAssets = "Deep Spruce #1C312A, Copper Accent #C87D55, Crisp Cream #F4F1EA. Fraunces + Inter."
                            deadline = "In 72 Hours"
                        },
                        label = { Text("☕ Coffee E-Com") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            clientName = "VoxelCloud Analytics"
                            projectTitle = "Kubernetes Infrastructure Dashboard"
                            rawBrief = "Cloud metric monitor with real-time streaming graph, node health status, incident alerting rules, and team seats."
                            brandAssets = "Cyber Slate #0A0F1D, Neon Green #10B981, Violet #8B5CF6. JetBrains Mono."
                            deadline = "In 4 Days"
                        },
                        label = { Text("📊 SaaS Dashboard") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            clientName = "Aura Dermatology Care"
                            projectTitle = "Specialist Clinic Booking Portal"
                            rawBrief = "Patient appointment booking with dermatologist bios, treatment options, calendar slot picker, and automated SMS/WhatsApp alerts."
                            brandAssets = "Rose Taupe #B76E79, Warm Ivory #FFFBF7, Slate Gray #334155. Playfair Display."
                            deadline = "In 36 Hours"
                        },
                        label = { Text("🩺 Clinic Portal") }
                    )
                }

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Client Name *") },
                    placeholder = { Text("e.g. Apex Dynamics Ltd.") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_name_input")
                )

                OutlinedTextField(
                    value = projectTitle,
                    onValueChange = { projectTitle = it },
                    label = { Text("Project Title *") },
                    placeholder = { Text("e.g. D2C Flagship Web Platform") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_title_input")
                )

                OutlinedTextField(
                    value = rawBrief,
                    onValueChange = { rawBrief = it },
                    label = { Text("Raw Client Brief & Requirements *") },
                    placeholder = { Text("Paste raw client specs, features, e-commerce, forms, authentication...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("raw_brief_input")
                )

                OutlinedTextField(
                    value = brandAssets,
                    onValueChange = { brandAssets = it },
                    label = { Text("Brand Assets, Colors & Typography") },
                    placeholder = { Text("e.g. Primary #1E1B4B, Accent #10B981, Montserrat/Inter") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("brand_assets_input")
                )

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Specified Deadline *") },
                    placeholder = { Text("e.g. In 48 Hours / 2026-09-08 18:00") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deadline_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (clientName.isNotBlank() && projectTitle.isNotBlank() && rawBrief.isNotBlank()) {
                        onSubmit(clientName, projectTitle, rawBrief, brandAssets, deadline, autoRunAiWorkflow)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (autoRunAiWorkflow) ElectricIndigo else MaterialTheme.colorScheme.primary
                ),
                enabled = clientName.isNotBlank() && projectTitle.isNotBlank() && rawBrief.isNotBlank(),
                modifier = Modifier.testTag("submit_order_button")
            ) {
                if (autoRunAiWorkflow) {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create & Auto-Execute with AI")
                } else {
                    Text("Execute Phase 1 Workflow")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
