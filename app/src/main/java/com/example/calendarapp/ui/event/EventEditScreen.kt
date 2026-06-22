package com.example.calendarapp.ui.event

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendarapp.data.model.Event
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    eventId: Int?,
    initialDate: Long,
    onNavigateBack: () -> Unit,
    viewModel: EventViewModel = viewModel()
) {
    val event by viewModel.event.collectAsState()

    LaunchedEffect(eventId) {
        if (eventId != null) {
            viewModel.loadEvent(eventId)
        }
    }

    var title by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(if (initialDate != 0L) initialDate else System.currentTimeMillis()) }

    LaunchedEffect(event) {
        event?.let {
            title = it.title
            memo = it.memo
            date = it.date
        }
    }

    val dateStr = remember(date) {
        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(date))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (eventId == null) "Add Event" else "Edit Event") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (eventId != null) {
                        IconButton(onClick = {
                            event?.let { viewModel.deleteEvent(it) }
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
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Date: $dateStr", style = MaterialTheme.typography.bodyMedium)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = memo,
                onValueChange = { memo = it },
                label = { Text("Memo") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    val newEvent = Event(
                        id = eventId ?: 0,
                        title = title,
                        date = date,
                        memo = memo
                    )
                    viewModel.saveEvent(newEvent)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}
