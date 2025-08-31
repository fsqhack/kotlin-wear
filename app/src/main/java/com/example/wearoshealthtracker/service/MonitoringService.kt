package com.example.wearoshealthtracker.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DeltaDataType
import com.example.wearoshealthtracker.data.HealthData
import com.example.wearoshealthtracker.network.ApiClient
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.guava.await
import java.util.concurrent.TimeUnit

class MonitoringService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val apiClient = ApiClient()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var tripId: String? = null
    private var userId: String? = null
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var currentHeartRate: Double = 0.0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        tripId = intent?.getStringExtra("TRIP_ID")
        userId = intent?.getStringExtra("USER_ID")

        if (tripId.isNullOrBlank() || userId.isNullOrBlank()) {
            Toast.makeText(this, "Trip ID or User ID is missing", Toast.LENGTH_LONG).show()
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            startForeground(1, createNotification())
            Toast.makeText(this, "Tracking started for $tripId", Toast.LENGTH_SHORT).show()
            startMonitoring()
        } catch (e: Exception) {
            Toast.makeText(this, "Error starting service: ${e.message}", Toast.LENGTH_LONG).show()
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    private fun startMonitoring() {
        if (tripId == null || userId == null) {
            stopSelf()
            return
        }

        // Start Heart Rate Monitoring
        startHeartRateMonitoring()

        // Start Location Monitoring
        startLocationUpdates()

        // Start periodic data sending
        serviceScope.launch {
            while (isActive) {
                delay(TimeUnit.SECONDS.toMillis(10)) // Send data every 10 seconds
                val data = HealthData(
                    tripId = tripId!!,
                    userId = userId!!,
                    latitude = currentLatitude,
                    longitude = currentLongitude,
                    heartRate = currentHeartRate
                )
                apiClient.postHealthData(data)
            }
        }
    }

    private fun startHeartRateMonitoring() {
        // Comment out actual heart rate monitoring code
        /*
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Using simulated heart rate data", Toast.LENGTH_LONG).show()
            startSimulatedHeartRateUpdates()
            return
        }

        val healthClient = HealthServices.getClient(this)
        val measureClient = healthClient.measureClient

        serviceScope.launch {
            try {
                val capabilities = measureClient.getCapabilitiesAsync().await()
                if (DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure) {
                    measureClient.registerMeasureCallback(
                        DataType.HEART_RATE_BPM,
                        HeartRateCallback()
                    )
                } else {
                    Toast.makeText(this@MonitoringService, "Device doesn't support heart rate monitoring", Toast.LENGTH_LONG).show()
                    startSimulatedHeartRateUpdates()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MonitoringService, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                startSimulatedHeartRateUpdates()
            }
        }
        */

        // Always use simulated heart rate data
        Toast.makeText(this, "Using simulated heart rate data", Toast.LENGTH_SHORT).show()
        startSimulatedHeartRateUpdates()
    }

    // New function to provide simulated heart rate data
    private fun startSimulatedHeartRateUpdates() {
        serviceScope.launch {
            while (isActive) {
                // Generate a realistic random heart rate between 60-100 BPM
                currentHeartRate = (60..100).random().toDouble()
                delay(3000) // Update every 3 seconds
            }
        }
    }

    private inner class HeartRateCallback : MeasureCallback {
        override fun onRegistered() {
            // Called when successfully registered
        }

        override fun onRegistrationFailed(throwable: Throwable) {
            // Called when registration fails
        }

        override fun onAvailabilityChanged(
            dataType: DeltaDataType<*, *>,
            availability: Availability
        ) {
            // This function is called when the availability of the data type changes.
        }

        override fun onDataReceived(data: DataPointContainer) {
            data.getData(DataType.HEART_RATE_BPM).lastOrNull()?.let {
                currentHeartRate = it.value
            }
        }
    }

    private fun startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    currentLatitude = it.latitude
                    currentLongitude = it.longitude
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "monitoring_channel"
        val channelName = "Monitoring Service"
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Health Tracker")
            .setContentText("Monitoring location and health data.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }
}
