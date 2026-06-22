package com.example.calendarapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendarapp.CalendarApp
import com.example.calendarapp.data.model.WorkShift
import com.example.calendarapp.ui.settings.dataStore
import com.example.calendarapp.util.SalaryCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

class DashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val db = (app as CalendarApp).database
    private val shiftDao = db.workShiftDao()
    private val workPlaceDao = db.workPlaceDao()
    private val dataStore = app.dataStore

    val selectedMonth = MutableStateFlow(YearMonth.now())

    private val annualGoalKey = doublePreferencesKey("annual_goal")
    private val deductTaxKey = androidx.datastore.preferences.core.booleanPreferencesKey("deduct_tax")

    val annualGoal: StateFlow<Double> = dataStore.data
        .map { it[annualGoalKey] ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val deductTax: StateFlow<Boolean> = dataStore.data
        .map { it[deductTaxKey] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val workPlaces = workPlaceDao.getAllWorkPlaces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyWage: StateFlow<Double> = combine(selectedMonth, workPlaces, deductTax) { month, places, tax ->
        Triple(month, places, tax)
    }.flatMapLatest { (month, places, tax) ->
        val start = month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = month.atEndOfMonth().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val placeMap = places.associateBy { it.id }
        shiftDao.getShiftsBetween(start, end).map { shifts ->
            SalaryCalculator.calculateMonthlyPay(shifts, placeMap, tax)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyWageByMonth: StateFlow<Map<YearMonth, Double>> = combine(workPlaces, deductTax) { places, tax ->
        Pair(places, tax)
    }.flatMapLatest { (places, tax) ->
        val placeMap = places.associateBy { it.id }
        shiftDao.getAllShifts().map { allShifts ->
            allShifts.groupBy { shift ->
                YearMonth.from(Instant.ofEpochMilli(shift.startDateTime).atZone(ZoneId.systemDefault()))
            }.mapValues { (_, shifts) ->
                SalaryCalculator.calculateMonthlyPay(shifts, placeMap, tax)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun selectMonth(month: YearMonth) { selectedMonth.value = month }
}
