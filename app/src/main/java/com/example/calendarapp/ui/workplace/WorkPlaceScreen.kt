package com.example.calendarapp.ui.workplace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendarapp.data.model.WorkPlace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkPlaceScreen(
    onNavigateBack: () -> Unit,
    viewModel: WorkPlaceViewModel = viewModel()
) {
    val workPlaces by viewModel.workPlaces.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingWorkPlace by remember { mutableStateOf<WorkPlace?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workplaces") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingWorkPlace = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Workplace")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(workPlaces) { workplace ->
                WorkPlaceItem(
                    workPlace = workplace,
                    onClick = {
                        editingWorkPlace = workplace
                        showDialog = true
                    },
                    onDelete = { viewModel.deleteWorkPlace(workplace) }
                )
            }
        }
    }

    if (showDialog) {
        WorkPlaceDialog(
            workPlace = editingWorkPlace,
            onDismiss = { showDialog = false },
            onSave = { wp ->
                viewModel.saveWorkPlace(wp)
                showDialog = false
            }
        )
    }
}

@Composable
fun WorkPlaceItem(workPlace: WorkPlace, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(workPlace.name, style = MaterialTheme.typography.titleMedium)
                Text("Base: ¥${workPlace.baseWage}/hr", style = MaterialTheme.typography.bodySmall)
                workPlace.nightWage?.let { Text("Night: ¥$it/hr", style = MaterialTheme.typography.bodySmall) }
                workPlace.holidayWage?.let { Text("Holiday: ¥$it/hr", style = MaterialTheme.typography.bodySmall) }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun WorkPlaceDialog(
    workPlace: WorkPlace?,
    onDismiss: () -> Unit,
    onSave: (WorkPlace) -> Unit
) {
    var name by remember { mutableStateOf(workPlace?.name ?: "") }
    var baseWage by remember { mutableStateOf(workPlace?.baseWage?.toString() ?: "") }
    var nightWage by remember { mutableStateOf(workPlace?.nightWage?.toString() ?: "") }
    var holidayWage by remember { mutableStateOf(workPlace?.holidayWage?.toString() ?: "") }
    var useHigherWage by remember { mutableStateOf(workPlace?.useHigherWage ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (workPlace == null) "Add Workplace" else "Edit Workplace") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = baseWage, onValueChange = { baseWage = it }, label = { Text("Base Wage (¥/hr)") })
                OutlinedTextField(value = nightWage, onValueChange = { nightWage = it }, label = { Text("Night Wage (¥/hr, optional)") })
                OutlinedTextField(value = holidayWage, onValueChange = { holidayWage = it }, label = { Text("Holiday Wage (¥/hr, optional)") })
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = useHigherWage, onCheckedChange = { useHigherWage = it })
                    Text("Use higher wage when night+holiday overlap")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        WorkPlace(
                            id = workPlace?.id ?: 0,
                            name = name,
                            baseWage = baseWage.toIntOrNull() ?: 0,
                            nightWage = nightWage.toIntOrNull(),
                            holidayWage = holidayWage.toIntOrNull(),
                            useHigherWage = useHigherWage
                        )
                    )
                },
                enabled = name.isNotBlank() && baseWage.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
