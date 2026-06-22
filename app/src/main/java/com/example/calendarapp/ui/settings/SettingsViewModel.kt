package com.example.calendarapp.ui.settings

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.dataStore

    companion object {
        val DEDUCT_TAX_KEY = booleanPreferencesKey("deduct_tax")
        val ANNUAL_GOAL_KEY = doublePreferencesKey("annual_goal")
    }

    val deductTax: StateFlow<Boolean> = dataStore.data
        .map { prefs -> prefs[DEDUCT_TAX_KEY] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val annualGoal: StateFlow<Double> = dataStore.data
        .map { prefs -> prefs[ANNUAL_GOAL_KEY] ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun setDeductTax(value: Boolean) {
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[DEDUCT_TAX_KEY] = value }
        }
    }

    fun setAnnualGoal(value: Double) {
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[ANNUAL_GOAL_KEY] = value }
        }
    }
}
