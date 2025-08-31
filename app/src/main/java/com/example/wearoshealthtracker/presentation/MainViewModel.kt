package com.example.wearoshealthtracker.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var tripId by mutableStateOf("")
    var userId by mutableStateOf("")
    var isMonitoring by mutableStateOf(false)

    fun onTripIdChange(value: String) {
        tripId = value
    }

    fun onUserIdChange(value: String) {
        userId = value
    }

    fun toggleMonitoring() {
        isMonitoring = !isMonitoring
    }
}
