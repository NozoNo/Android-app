package com.example.calendarapp.ui.shift

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendarapp.CalendarApp
import com.example.calendarapp.data.model.WorkPlace
import com.example.calendarapp.data.model.WorkShift
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShiftViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as CalendarApp).database
    private val workShiftDao = db.workShiftDao()
    private val workPlaceDao = db.workPlaceDao()

    val workPlaces: StateFlow<List<WorkPlace>> = workPlaceDao.getAllWorkPlaces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _shift = MutableStateFlow<WorkShift?>(null)
    val shift: StateFlow<WorkShift?> = _shift

    fun loadShift(id: Int) {
        viewModelScope.launch {
            _shift.value = workShiftDao.getShiftById(id)
        }
    }

    fun saveShift(shift: WorkShift) {
        viewModelScope.launch {
            if (shift.id == 0) {
                workShiftDao.insertShift(shift)
            } else {
                workShiftDao.updateShift(shift)
            }
        }
    }

    fun deleteShift(shift: WorkShift) {
        viewModelScope.launch {
            workShiftDao.deleteShift(shift)
        }
    }
}
