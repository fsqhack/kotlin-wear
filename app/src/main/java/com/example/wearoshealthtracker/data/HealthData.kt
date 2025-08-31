package com.example.wearoshealthtracker.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthData(
    @SerialName("trip_id")
    val tripId: String,
    @SerialName("user_id")
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("heart_rate")
    val heartRate: Double
)
