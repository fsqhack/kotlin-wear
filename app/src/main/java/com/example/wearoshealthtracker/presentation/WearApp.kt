package com.example.wearoshealthtracker.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import com.example.wearoshealthtracker.service.MonitoringService
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun WearApp(
    context: Context,
    viewModel: MainViewModel = viewModel()
) {
    // Only request location permission since we're mocking heart rate data
    val permissions = listOf(
        // Removed BODY_SENSORS since we're mocking that data
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        // Log permissions result for debugging
        permissionsMap.forEach { (permission, isGranted) ->
            val shortName = permission.substringAfterLast(".")
            Toast.makeText(context, "$shortName: $isGranted", Toast.LENGTH_SHORT).show()
        }

        // Always start the service, regardless of permission results (for development)
        try {
            val intent = Intent(context, MonitoringService::class.java).apply {
                putExtra("TRIP_ID", viewModel.tripId)
                putExtra("USER_ID", viewModel.userId)
            }
            context.startForegroundService(intent)
            viewModel.toggleMonitoring()
        } catch (e: Exception) {
            Toast.makeText(context, "Error starting service: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // The rest of the permission handling code can be kept for reference but won't block the app
        if (!permissionsMap.values.all { it }) {
            Toast.makeText(
                context,
                "Some permissions were denied. Using mock data where needed.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Simplified permission checking function
    fun checkAndRequestPermissions() {        
        // Request permissions directly - don't check first on Wear OS
        // This approach avoids multiple toast messages that can be confusing on the small screen
        permissionLauncher.launch(permissions.toTypedArray())
    }

    Scaffold(
        timeText = { TimeText() }
    ) {
        if (viewModel.isMonitoring) {
            MonitoringScreen(onStopClick = {
                viewModel.toggleMonitoring()
                context.stopService(Intent(context, MonitoringService::class.java))
            })
        } else {
            InputScreen(
                tripId = viewModel.tripId,
                userId = viewModel.userId,
                onTripIdChange = viewModel::onTripIdChange,
                onUserIdChange = viewModel::onUserIdChange,
                onStartClick = {
                    checkAndRequestPermissions()
                }
            )
        }
    }
}

@Composable
fun InputScreen(
    tripId: String,
    userId: String,
    onTripIdChange: (String) -> Unit,
    onUserIdChange: (String) -> Unit,
    onStartClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Trip ID")
        OutlinedChip(
            onClick = { onTripIdChange("trip-1") },
            label = { Text(tripId.ifEmpty { "Enter Trip ID" }) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("User ID")
        OutlinedChip(
            onClick = { onUserIdChange("kabirrajsingh10@gmail.com") },
            label = { Text(userId.ifEmpty { "Enter User ID" }) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onStartClick, enabled = tripId.isNotBlank() && userId.isNotBlank()) {
            Text("Start")
        }
    }
}

@Composable
fun MonitoringScreen(onStopClick: () -> Unit) {
    // Add timer state to track elapsed seconds
    var elapsedSeconds by remember { mutableStateOf(0L) }
    
    // Timer effect that updates the elapsed time every second
    LaunchedEffect(Unit) {
        val startTimeMillis = System.currentTimeMillis()
        while (true) {
            val currentTimeMillis = System.currentTimeMillis()
            elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis - startTimeMillis)
            delay(1000) // Update every second
        }
    }
    
    // Format the elapsed time as MM:SS
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tracking Data...", textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Add the timer display
        Text(
            text = formattedTime,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Make the button oval shaped
        Button(
            onClick = onStopClick,
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
            shape = CircleShape,
            modifier = Modifier.size(width = 120.dp, height = 50.dp)
        ) {
            Text("Stop tracking")
        }
    }
}