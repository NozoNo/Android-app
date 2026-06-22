package com.example.calendarapp.ui.workplace

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendarapp.CalendarApp
import com.example.calendarapp.data.model.WorkPlace
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkPlaceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as CalendarApp).database
    private val workPlaceDao = db.workPlaceDao()

    val workPlaces: StateFlow<List<WorkPlace>> = workPlaceDao.getAllWorkPlaces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadWorkPlace(id: Int, onLoaded: (WorkPlace?) -> Unit) {
        viewModelScope.launch {
            onLoaded(workPlaceDao.getWorkPlaceById(id))
        }
    }

    fun saveWorkPlace(workPlace: WorkPlace) {
        viewModelScope.launch {
            if (workPlace.id == 0) {
                workPlaceDao.insertWorkPlace(workPlace)
            } else {
                workPlaceDao.updateWorkPlace(workPlace)
            }
        }
    }

    fun deleteWorkPlace(workPlace: WorkPlace) {
        viewModelScope.launch {
            workPlaceDao.deleteWorkPlace(workPlace)
        }
    }
}
