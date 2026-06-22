package com.example.calendarapp.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToWorkPlace: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val deductTax by viewModel.deductTax.collectAsState()
    val annualGoal by viewModel.annualGoal.collectAsState()
    var annualGoalText by remember { mutableStateOf("") }

    LaunchedEffect(annualGoal) {
        if (annualGoal > 0) annualGoalText = annualGoal.toLong().toString()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Deduction Mode", style = MaterialTheme.typography.titleMedium)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = !deductTax, onClick = { viewModel.setDeductTax(false) })
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Mode A: No Deduction")
                        Text(
                            "Hourly x actual work hours",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = deductTax, onClick = { viewModel.setDeductTax(true) })
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Mode B: Estimate Tax & Insurance")
                        Text(
                            "Subtract estimated income tax + social insurance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Annual Goal", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = annualGoalText,
                    onValueChange = { annualGoalText = it },
                    label = { Text("Annual Goal (¥)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { viewModel.setAnnualGoal(annualGoalText.toDoubleOrNull() ?: 0.0) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Goal")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("Workplaces") },
                supportingContent = { Text("Manage workplace wage settings") },
                leadingContent = { Icon(Icons.Default.Business, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onNavigateToWorkPlace)
            )
        }
    }
}
