package com.example.calendarapp.ui.shift

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendarapp.data.model.WorkShift
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftInputScreen(
    shiftId: Int?,
    initialDate: Long,
    onNavigateBack: () -> Unit,
    viewModel: ShiftViewModel = viewModel()
) {
    val shift by viewModel.shift.collectAsState()
    val workPlaces by viewModel.workPlaces.collectAsState()

    LaunchedEffect(shiftId) {
        if (shiftId != null) viewModel.loadShift(shiftId)
    }

    val baseTime = if (initialDate != 0L) initialDate else System.currentTimeMillis()
    val baseCal = Calendar.getInstance().apply { timeInMillis = baseTime }

    var selectedWorkPlaceId by remember { mutableStateOf<Int?>(null) }
    var startHour by remember { mutableStateOf(baseCal.get(Calendar.HOUR_OF_DAY)) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(baseCal.get(Calendar.HOUR_OF_DAY) + 4) }
    var endMinute by remember { mutableStateOf(0) }
    var breakMinutes by remember { mutableStateOf("0") }
    var memo by remember { mutableStateOf("") }
    var workPlaceExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(shift) {
        shift?.let { s ->
            selectedWorkPlaceId = s.workPlaceId
            val startCal = Calendar.getInstance().apply { timeInMillis = s.startDateTime }
            val endCal = Calendar.getInstance().apply { timeInMillis = s.endDateTime }
            startHour = startCal.get(Calendar.HOUR_OF_DAY)
            startMinute = startCal.get(Calendar.MINUTE)
            endHour = endCal.get(Calendar.HOUR_OF_DAY)
            endMinute = endCal.get(Calendar.MINUTE)
            breakMinutes = s.breakMinutes.toString()
            memo = s.memo
        }
    }

    LaunchedEffect(workPlaces) {
        if (selectedWorkPlaceId == null && workPlaces.isNotEmpty()) {
            selectedWorkPlaceId = workPlaces.first().id
        }
    }

    val selectedWorkPlace = workPlaces.find { it.id == selectedWorkPlaceId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (shiftId == null) "Add Shift" else "Edit Shift") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (shiftId != null) {
                        IconButton(onClick = {
                            shift?.let { viewModel.deleteShift(it) }
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = workPlaceExpanded,
                onExpandedChange = { workPlaceExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedWorkPlace?.name ?: "Select Workplace",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Workplace") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = workPlaceExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = workPlaceExpanded,
                    onDismissRequest = { workPlaceExpanded = false }
                ) {
                    workPlaces.forEach { wp ->
                        DropdownMenuItem(
                            text = { Text(wp.name) },
                            onClick = {
                                selectedWorkPlaceId = wp.id
                                workPlaceExpanded = false
                            }
                        )
                    }
                }
            }

            Text("Start Time", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startHour.toString().padStart(2, '0'),
                    onValueChange = { startHour = it.toIntOrNull()?.coerceIn(0, 23) ?: startHour },
                    label = { Text("Hour") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = startMinute.toString().padStart(2, '0'),
                    onValueChange = { startMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: startMinute },
                    label = { Text("Minute") },
                    modifier = Modifier.weight(1f)
                )
            }

            Text("End Time", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = endHour.toString().padStart(2, '0'),
                    onValueChange = { endHour = it.toIntOrNull()?.coerceIn(0, 47) ?: endHour },
                    label = { Text("Hour") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = endMinute.toString().padStart(2, '0'),
                    onValueChange = { endMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: endMinute },
                    label = { Text("Minute") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = breakMinutes,
                onValueChange = { breakMinutes = it },
                label = { Text("Break Minutes") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = memo,
                onValueChange = { memo = it },
                label = { Text("Memo") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Button(
                onClick = {
                    val startCal = Calendar.getInstance().apply {
                        timeInMillis = baseTime
                        set(Calendar.HOUR_OF_DAY, startHour)
                        set(Calendar.MINUTE, startMinute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val endCal = Calendar.getInstance().apply {
                        timeInMillis = baseTime
                        set(Calendar.HOUR_OF_DAY, endHour % 24)
                        set(Calendar.MINUTE, endMinute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        if (endHour >= 24) add(Calendar.DAY_OF_MONTH, 1)
                    }
                    val newShift = WorkShift(
                        id = shiftId ?: 0,
                        workPlaceId = selectedWorkPlaceId ?: return@Button,
                        startDateTime = startCal.timeInMillis,
                        endDateTime = endCal.timeInMillis,
                        breakMinutes = breakMinutes.toIntOrNull() ?: 0,
                        memo = memo
                    )
                    viewModel.saveShift(newShift)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedWorkPlaceId != null
            ) {
                Text("Save")
            }
        }
    }
}
