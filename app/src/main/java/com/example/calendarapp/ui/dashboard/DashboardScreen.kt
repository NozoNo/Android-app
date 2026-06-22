package com.example.calendarapp.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar

@Composable
fun DashboardScreen(
    onNavigateToWorkPlace: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val monthlySalaries by viewModel.monthlySalaries.collectAsState()
    val currentYearTotal by viewModel.currentYearTotal.collectAsState()
    val annualGoal by viewModel.annualGoal.collectAsState()
    val currentMonth = monthlySalaries.lastOrNull()?.second ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("This Month", style = MaterialTheme.typography.titleMedium)
                Text(
                    "¥${"%,.0f".format(currentMonth)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (annualGoal > 0) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Annual Progress", style = MaterialTheme.typography.titleMedium)
                    Text("¥${"%,.0f".format(currentYearTotal)} / ¥${"%,.0f".format(annualGoal)}")
                    val progress = (currentYearTotal / annualGoal).toFloat().coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(16.dp)
                    )
                    Text("${(progress * 100).toInt()}% of annual goal")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Monthly Salary (6 months)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (monthlySalaries.isNotEmpty()) {
                    val maxVal = monthlySalaries.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1.0
                    BarChart(
                        data = monthlySalaries,
                        maxValue = maxVal,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                } else {
                    Text("No data yet", color = Color.Gray)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Year Total (${Calendar.getInstance().get(Calendar.YEAR)})",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "¥${"%,.0f".format(currentYearTotal)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BarChart(
    data: List<Pair<String, Double>>,
    maxValue: Double,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val barWidth = size.width / (data.size * 2f)
        val chartHeight = size.height - 40f

        data.forEachIndexed { index, (_, value) ->
            val barHeight = if (maxValue > 0) (value / maxValue * chartHeight).toFloat() else 0f
            val left = index * (barWidth * 2) + barWidth / 2
            val top = chartHeight - barHeight

            drawRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight)
            )
        }
    }
}
