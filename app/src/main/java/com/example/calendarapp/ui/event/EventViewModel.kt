package com.example.calendarapp.ui.event

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendarapp.CalendarApp
import com.example.calendarapp.data.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as CalendarApp).database
    private val eventDao = db.eventDao()

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event

    fun loadEvent(id: Int) {
        viewModelScope.launch {
            _event.value = eventDao.getEventById(id)
        }
    }

    fun saveEvent(event: Event) {
        viewModelScope.launch {
            if (event.id == 0) {
                eventDao.insertEvent(event)
            } else {
                eventDao.updateEvent(event)
            }
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            eventDao.deleteEvent(event)
        }
    }
}
