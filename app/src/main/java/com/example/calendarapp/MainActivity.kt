package com.example.calendarapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.calendarapp.ui.calendar.CalendarScreen
import com.example.calendarapp.ui.dashboard.DashboardScreen
import com.example.calendarapp.ui.event.EventEditScreen
import com.example.calendarapp.ui.settings.SettingsScreen
import com.example.calendarapp.ui.shift.ShiftInputScreen
import com.example.calendarapp.ui.theme.CalendarAppTheme
import com.example.calendarapp.ui.workplace.WorkPlaceScreen

sealed class Screen(val route: String) {
    object Calendar : Screen("calendar")
    object Dashboard : Screen("dashboard")
    object Settings : Screen("settings")
    object EventEdit : Screen("event_edit?eventId={eventId}&date={date}") {
        fun createRoute(eventId: Int = -1, date: Long = 0L) = "event_edit?eventId=$eventId&date=$date"
    }
    object ShiftInput : Screen("shift_input?shiftId={shiftId}&date={date}") {
        fun createRoute(shiftId: Int = -1, date: Long = 0L) = "shift_input?shiftId=$shiftId&date=$date"
    }
    object WorkPlace : Screen("workplace")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalendarAppTheme {
                CalendarAppNavigation()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarAppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        Triple(Screen.Calendar.route, "Calendar", Icons.Default.CalendarMonth),
        Triple(Screen.Dashboard.route, "Dashboard", Icons.Default.Dashboard),
        Triple(Screen.Settings.route, "Settings", Icons.Default.Settings)
    )

    val showBottomBar = currentDestination?.route?.let { route ->
        route == Screen.Calendar.route || route == Screen.Dashboard.route || route == Screen.Settings.route
    } ?: true

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Calendar.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Screen.EventEdit.createRoute(eventId = eventId))
                    },
                    onShiftClick = { shiftId ->
                        navController.navigate(Screen.ShiftInput.createRoute(shiftId = shiftId))
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToWorkPlace = { navController.navigate(Screen.WorkPlace.route) }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToWorkPlace = { navController.navigate(Screen.WorkPlace.route) }
                )
            }
            composable(
                route = Screen.EventEdit.route,
                arguments = listOf(
                    androidx.navigation.navArgument("eventId") {
                        type = androidx.navigation.NavType.IntType
                        defaultValue = -1
                    },
                    androidx.navigation.navArgument("date") {
                        type = androidx.navigation.NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getInt("eventId") ?: -1
                val date = backStackEntry.arguments?.getLong("date") ?: 0L
                EventEditScreen(
                    eventId = if (eventId == -1) null else eventId,
                    initialDate = date,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.ShiftInput.route,
                arguments = listOf(
                    androidx.navigation.navArgument("shiftId") {
                        type = androidx.navigation.NavType.IntType
                        defaultValue = -1
                    },
                    androidx.navigation.navArgument("date") {
                        type = androidx.navigation.NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val shiftId = backStackEntry.arguments?.getInt("shiftId") ?: -1
                val date = backStackEntry.arguments?.getLong("date") ?: 0L
                ShiftInputScreen(
                    shiftId = if (shiftId == -1) null else shiftId,
                    initialDate = date,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.WorkPlace.route) {
                WorkPlaceScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add") },
                text = {
                    Column {
                        TextButton(onClick = {
                            showAddDialog = false
                            navController.navigate(Screen.EventEdit.createRoute())
                        }) {
                            Text("Add Event")
                        }
                        TextButton(onClick = {
                            showAddDialog = false
                            navController.navigate(Screen.ShiftInput.createRoute())
                        }) {
                            Text("Add Work Shift")
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
