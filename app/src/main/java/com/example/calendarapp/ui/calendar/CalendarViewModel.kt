package com.example.calendarapp.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendarapp.CalendarApp
import com.example.calendarapp.data.model.Event
import com.example.calendarapp.data.model.WorkPlace
import com.example.calendarapp.data.model.WorkShift
import com.example.calendarapp.util.SalaryCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as CalendarApp).database
    private val eventDao = db.eventDao()
    private val workShiftDao = db.workShiftDao()
    private val workPlaceDao = db.workPlaceDao()

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance())
    val selectedMonth: StateFlow<Calendar> = _selectedMonth

    val events: StateFlow<List<Event>> = eventDao.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shifts: StateFlow<List<WorkShift>> = workShiftDao.getAllShifts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workPlaces: StateFlow<List<WorkPlace>> = workPlaceDao.getAllWorkPlaces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlySalary: StateFlow<Double> = combine(shifts, workPlaces, _selectedMonth) { shiftList, places, month ->
        val startCal = Calendar.getInstance().apply {
            set(month.get(Calendar.YEAR), month.get(Calendar.MONTH), 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(month.get(Calendar.YEAR), month.get(Calendar.MONTH) + 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val monthShifts = shiftList.filter { it.startDateTime >= startCal.timeInMillis && it.startDateTime < endCal.timeInMillis }
        val placesMap = places.associateBy { it.id }
        SalaryCalculator.calculateMonthlyPay(monthShifts, placesMap, false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun setMonth(year: Int, month: Int) {
        _selectedMonth.value = Calendar.getInstance().apply {
            set(year, month, 1)
        }
    }
}
