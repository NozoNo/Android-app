package com.example.calendarapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Work
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
    var goalInput by remember(annualGoal) { mutableStateOf(if (annualGoal > 0) annualGoal.toLong().toString() else "") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Deduction mode
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Deduction Mode", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (deductTax) "Mode B (Estimate Tax)" else "Mode A (No Deduction)")
                            Text(
                                if (deductTax) "Income tax & social insurance estimated" else "Gross pay only",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = deductTax, onCheckedChange = { viewModel.setDeductTax(it) })
                    }
                }
            }

            // Annual goal
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Annual Goal (¥)", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = goalInput,
                            onValueChange = { if (it.all(Char::isDigit)) goalInput = it },
                            label = { Text("Goal amount") },
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = {
                            viewModel.setAnnualGoal(goalInput.toDoubleOrNull() ?: 0.0)
                        }) { Text("Set") }
                    }
                }
            }

            // Manage workplaces
            OutlinedButton(
                onClick = onNavigateToWorkPlace,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Work, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Manage Workplaces")
            }
        }
    }
}
