package com.example.calendarapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendarapp.CalendarApp
import com.example.calendarapp.data.model.WorkPlace
import com.example.calendarapp.data.model.WorkShift
import com.example.calendarapp.ui.settings.dataStore
import com.example.calendarapp.util.SalaryCalculator
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import kotlinx.coroutines.flow.*
import java.util.Calendar

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as CalendarApp).database
    private val workShiftDao = db.workShiftDao()
    private val workPlaceDao = db.workPlaceDao()
    private val dataStore = application.dataStore

    val deductTax: StateFlow<Boolean> = dataStore.data
        .map { it[booleanPreferencesKey("deduct_tax")] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val annualGoal: StateFlow<Double> = dataStore.data
        .map { it[doublePreferencesKey("annual_goal")] ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val shifts: StateFlow<List<WorkShift>> = workShiftDao.getAllShifts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workPlaces: StateFlow<List<WorkPlace>> = workPlaceDao.getAllWorkPlaces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlySalaries: StateFlow<List<Pair<String, Double>>> = combine(shifts, workPlaces, deductTax) { shiftList, places, deduct ->
        val placesMap = places.associateBy { it.id }
        (5 downTo 0).map { monthsAgo ->
            val cal = Calendar.getInstance().apply {
                add(Calendar.MONTH, -monthsAgo)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                timeInMillis = cal.timeInMillis
                add(Calendar.MONTH, 1)
            }
            val monthShifts = shiftList.filter {
                it.startDateTime >= cal.timeInMillis && it.startDateTime < endCal.timeInMillis
            }
            val label = "${cal.get(Calendar.YEAR)}/${(cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}"
            label to SalaryCalculator.calculateMonthlyPay(monthShifts, placesMap, deduct)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentYearTotal: StateFlow<Double> = combine(shifts, workPlaces, deductTax) { shiftList, places, deduct ->
        val placesMap = places.associateBy { it.id }
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val startCal = Calendar.getInstance().apply {
            set(year, 0, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(year + 1, 0, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val yearShifts = shiftList.filter {
            it.startDateTime >= startCal.timeInMillis && it.startDateTime < endCal.timeInMillis
        }
        SalaryCalculator.calculateMonthlyPay(yearShifts, placesMap, deduct)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}
