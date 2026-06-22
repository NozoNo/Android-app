package com.example.calendarapp.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendarapp.viewmodel.DashboardViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToWorkPlace: () -> Unit,
    vm: DashboardViewModel = viewModel()
) {
    val selectedMonth by vm.selectedMonth.collectAsStateWithLifecycle()
    val monthlyWage by vm.monthlyWage.collectAsStateWithLifecycle()
    val monthlyWageByMonth by vm.monthlyWageByMonth.collectAsStateWithLifecycle()
    val annualGoal by vm.annualGoal.collectAsStateWithLifecycle()

    val annualTotal = monthlyWageByMonth.values.sum()
    val goalProgress = if (annualGoal > 0) (annualTotal / annualGoal).coerceIn(0.0, 1.0) else 0.0

    // Last 6 months for chart
    val chartMonths = (5 downTo 0).map { selectedMonth.minusMonths(it.toLong()) }
    val chartMax = chartMonths.maxOfOrNull { monthlyWageByMonth[it] ?: 0.0 }?.takeIf { it > 0 } ?: 1.0

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("給料ダッシュボード") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Month selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.selectMonth(selectedMonth.minusMonths(1)) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                    }
                    Text(
                        selectedMonth.format(DateTimeFormatter.ofPattern("yyyy年M月")),
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { vm.selectMonth(selectedMonth.plusMonths(1)) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                    }
                }
            }

            item {
                // Monthly salary card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("今月の給与", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "¥${monthlyWage.roundToInt().formatNumber()}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            item {
                // Bar chart
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("月別給与（過去6ヶ月）", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val barColor = MaterialTheme.colorScheme.primary
                            val selectedBarColor = MaterialTheme.colorScheme.secondary
                            chartMonths.forEach { month ->
                                val wage = monthlyWageByMonth[month] ?: 0.0
                                val ratio = (wage / chartMax).toFloat()
                                val isSelected = month == selectedMonth
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Canvas(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp)
                                    ) {
                                        val barHeight = size.height * ratio
                                        drawRect(
                                            color = if (isSelected) selectedBarColor else barColor,
                                            topLeft = Offset(0f, size.height - barHeight),
                                            size = Size(size.width, barHeight)
                                        )
                                    }
                                    Text(
                                        month.format(DateTimeFormatter.ofPattern("M月")),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (annualGoal > 0) {
                item {
                    // Wall gauge (annual progress)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("年間目標達成率", style = MaterialTheme.typography.labelMedium)
                                Text("¥${annualTotal.roundToInt().formatNumber()} / ¥${annualGoal.roundToInt().formatNumber()}")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { goalProgress.toFloat() },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${(goalProgress * 100).roundToInt()}%", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = onNavigateToWorkPlace,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Work, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("バイト先を管理")
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

private fun Int.formatNumber(): String {
    return "%,d".format(this)
}
