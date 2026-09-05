package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertRed

@Composable
fun BottleneckDialog(
    projectTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (bottleneck: String, mitigation: String) -> Unit
) {
    var bottleneckText by remember { mutableStateOf("") }
    var mitigationText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "🚨 Report Bottleneck & Alert Host",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AlertRed
                )
                Text(
                    text = "Project: $projectTitle\nMandate: Dynamic reprioritization & emergency WhatsApp notification with mitigation plan",
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
                Text(
                    text = "Common Scenarios:",
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
                            bottleneckText = "Stripe production API keys delayed by client finance team."
                            mitigationText = "Deploy verified mock transaction engine with hot-swap configuration variable so client can inject live keys in 1-click at launch without delaying code delivery."
                        },
                        label = { Text("API Delay") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            bottleneckText = "Client provided low-resolution bitmap logos instead of vectors."
                            mitigationText = "Autonomous vectorization via high-precision SVG tracing and WebP lossless compression to preserve 4K retina fidelity."
                        },
                        label = { Text("Asset Resolution") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            bottleneckText = "Complex multi-step checkout edge case taking 2h longer than estimated."
                            mitigationText = "Reallocated frontend testing capacity and parallelized responsive QA review to maintain target milestone completion time."
                        },
                        label = { Text("Schedule Drift") }
                    )
                }

                OutlinedTextField(
                    value = bottleneckText,
                    onValueChange = { bottleneckText = it },
                    label = { Text("Identified Bottleneck *") },
                    placeholder = { Text("Describe the blocker or delay factor...") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bottleneck_text_input")
                )

                OutlinedTextField(
                    value = mitigationText,
                    onValueChange = { mitigationText = it },
                    label = { Text("Mitigation Plan *") },
                    placeholder = { Text("Actionable resolution ensuring deadline guarantee...") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mitigation_text_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (bottleneckText.isNotBlank() && mitigationText.isNotBlank()) {
                        onSubmit(bottleneckText, mitigationText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                enabled = bottleneckText.isNotBlank() && mitigationText.isNotBlank(),
                modifier = Modifier.testTag("submit_bottleneck_button")
            ) {
                Text("Dispatch Alert to Host WhatsApp")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
