package com.example.calendarapp.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.example.calendarapp.data.model.Event
import com.example.calendarapp.data.model.WorkShift
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    onEventClick: (Int) -> Unit,
    onShiftClick: (Int) -> Unit,
    viewModel: CalendarViewModel = viewModel()
) {
    val events by viewModel.events.collectAsState()
    val shifts by viewModel.shifts.collectAsState()
    val monthlySalary by viewModel.monthlySalary.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(24) }
    val endMonth = remember { currentMonth.plusMonths(24) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    LaunchedEffect(calendarState.firstVisibleMonth) {
        val ym = calendarState.firstVisibleMonth.yearMonth
        viewModel.setMonth(ym.year, ym.monthValue - 1)
    }

    val eventsOnDate = remember(events, selectedDate) {
        events.filter { event ->
            java.time.Instant.ofEpochMilli(event.date)
                .atZone(ZoneId.systemDefault()).toLocalDate() == selectedDate
        }
    }

    val shiftsOnDate = remember(shifts, selectedDate) {
        shifts.filter { shift ->
            java.time.Instant.ofEpochMilli(shift.startDateTime)
                .atZone(ZoneId.systemDefault()).toLocalDate() == selectedDate
        }
    }

    val eventsByDate = remember(events) {
        events.groupBy { event ->
            java.time.Instant.ofEpochMilli(event.date)
                .atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    val shiftsByDate = remember(shifts) {
        shifts.groupBy { shift ->
            java.time.Instant.ofEpochMilli(shift.startDateTime)
                .atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = "Monthly Salary: ¥${"%,.0f".format(monthlySalary)}",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalCalendar(
            state = calendarState,
            dayContent = { day ->
                DayCell(
                    day = day,
                    isSelected = day.date == selectedDate,
                    hasEvent = eventsByDate.containsKey(day.date),
                    hasShift = shiftsByDate.containsKey(day.date),
                    onClick = { if (day.position == DayPosition.MonthDate) selectedDate = day.date }
                )
            },
            monthHeader = { month ->
                MonthHeader(month.yearMonth)
            }
        )

        HorizontalDivider()

        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            if (eventsOnDate.isNotEmpty()) {
                item {
                    Text(
                        "Events",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(eventsOnDate) { event ->
                    EventItem(event = event, onClick = { onEventClick(event.id) })
                }
            }
            if (shiftsOnDate.isNotEmpty()) {
                item {
                    Text(
                        "Work Shifts",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(shiftsOnDate) { shift ->
                    ShiftItem(shift = shift, onClick = { onShiftClick(shift.id) })
                }
            }
            if (eventsOnDate.isEmpty() && shiftsOnDate.isEmpty()) {
                item {
                    Text(
                        "No events or shifts on this day",
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun MonthHeader(yearMonth: YearMonth) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = yearMonth.format(formatter),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun DayCell(
    day: CalendarDay,
    isSelected: Boolean,
    hasEvent: Boolean,
    hasShift: Boolean,
    onClick: () -> Unit
) {
    val isToday = day.date == LocalDate.now()
    val textColor = when {
        day.position != DayPosition.MonthDate -> Color.LightGray
        day.date.dayOfWeek.value == 7 -> Color.Red
        day.date.dayOfWeek.value == 6 -> Color.Blue
        else -> Color.Black
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = if (isSelected) Color.White else textColor,
                fontSize = 14.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (hasEvent) {
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFFFF9800)))
                }
                if (hasShift) {
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                }
            }
        }
    }
}

@Composable
fun EventItem(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(event.title, fontWeight = FontWeight.Medium)
            if (event.memo.isNotEmpty()) {
                Text(event.memo, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ShiftItem(shift: WorkShift, onClick: () -> Unit) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val startTime = java.time.Instant.ofEpochMilli(shift.startDateTime)
        .atZone(ZoneId.systemDefault()).toLocalTime().format(timeFormatter)
    val endTime = java.time.Instant.ofEpochMilli(shift.endDateTime)
        .atZone(ZoneId.systemDefault()).toLocalTime().format(timeFormatter)

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("$startTime - $endTime", fontWeight = FontWeight.Medium)
            if (shift.memo.isNotEmpty()) {
                Text(shift.memo, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}
