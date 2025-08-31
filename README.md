# WearOS Health Tracker

## Overview
This WearOS application monitors a user's location and heart rate data and sends it to a remote server. The app is designed for tracking health and location data during trips, with each session identified by a Trip ID and User ID.

## Features
- Input Trip ID and User ID to start a monitoring session
- Monitors user's heart rate using the device's heart rate sensor
- Tracks user's location using GPS
- Sends collected data to a remote server at regular intervals
- Background service continues monitoring even when the app is not in the foreground
- Permissions management for location and body sensors

## Requirements
- WearOS device with heart rate sensor
- Android Studio Arctic Fox or newer
- Minimum SDK: 26 (Android 8.0)
- Target SDK: 33 (Android 13)

## Setup Instructions
1. Clone this repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run on a WearOS device or emulator

## Project Structure
- **MainActivity.kt**: Entry point for the application
- **WearApp.kt**: Main UI components and app navigation
- **MainViewModel.kt**: State management for the UI
- **MonitoringService.kt**: Background service for monitoring health and location data
- **HealthData.kt**: Data model for the payload sent to the server
- **ApiClient.kt**: Network client for communicating with the remote server

## How It Works
1. The app starts with an input screen where users can enter Trip ID and User ID
2. When the "Start" button is pressed, the app requests necessary permissions
3. Once permissions are granted, a foreground service starts monitoring:
   - Heart rate data via Health Services API
   - Location data via FusedLocationProviderClient
4. The collected data is sent to `https://fsq-ai-server-20555262314.us-central1.run.app/health/` every 10 seconds
5. The monitoring continues until the user presses the "Stop" button

## Permissions
The app requires the following permissions:
- `BODY_SENSORS`: To access the device's heart rate sensor
- `ACCESS_FINE_LOCATION`: To get precise location data
- `ACCESS_COARSE_LOCATION`: As a fallback for location data
- `INTERNET`: To send data to the remote server
- `FOREGROUND_SERVICE`: To run the monitoring service in the background

## Testing
For testing in an emulator:
1. The app automatically fills in "test_trip_1" and "test_user_1" when tapping on the input fields
2. In a real device, a proper input mechanism would be implemented

## Dependencies
- Jetpack Compose for WearOS UI
- Health Services for heart rate monitoring
- Google Play Services Location for GPS tracking
- Ktor for network requests
- Kotlinx Serialization for JSON parsing
- Kotlinx Coroutines for asynchronous operations

## Notes
- For a production app, consider implementing:
  - Better error handling
  - Data persistence
  - More robust text input on WearOS
  - Additional health metrics
  - Battery optimization
