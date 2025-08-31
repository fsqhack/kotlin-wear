package com.example.wearoshealthtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.wearoshealthtracker.presentation.WearApp
import com.example.wearoshealthtracker.presentation.theme.WearOSHealthTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearOSHealthTrackerTheme {
                WearApp(context = this)
            }
        }
    }
}
