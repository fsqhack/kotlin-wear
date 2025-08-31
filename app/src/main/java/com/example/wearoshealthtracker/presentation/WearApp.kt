package com.example.wearoshealthtracker.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import com.example.wearoshealthtracker.service.MonitoringService

@Composable
fun WearApp(
    context: Context,
    viewModel: MainViewModel = viewModel()
) {
    val permissions = listOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        if (permissionsMap.values.all { it }) {
            viewModel.toggleMonitoring()
            val intent = Intent(context, MonitoringService::class.java).apply {
                putExtra("TRIP_ID", viewModel.tripId)
                putExtra("USER_ID", viewModel.userId)
            }
            context.startForegroundService(intent)
        }
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
                    permissionLauncher.launch(permissions.toTypedArray())
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
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Trip ID")
        OutlinedChip(onClick = { /* For focus */ }, label = { Text(tripId.ifEmpty { "Enter Trip ID" }) })
        // In a real app, you'd use an input method like OutlinedTextField, but that's more complex on Wear.
        // This is a simplified representation. You'd typically launch a remote input intent.
        // For simplicity, we'll assume IDs are pre-filled or handled differently.

        Spacer(modifier = Modifier.height(8.dp))

        Text("User ID")
        OutlinedChip(onClick = { /* For focus */ }, label = { Text(userId.ifEmpty { "Enter User ID" }) })

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onStartClick, enabled = tripId.isNotBlank() && userId.isNotBlank()) {
            Text("Start")
        }
    }
}

@Composable
fun MonitoringScreen(onStopClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Monitoring...", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStopClick, colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)) {
            Text("Stop")
        }
    }
}