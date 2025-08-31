package com.example.wearoshealthtracker.network

import com.example.wearoshealthtracker.data.HealthData
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    private val url = "https://fsq-ai-server-20555262314.us-central1.run.app/health/"

    suspend fun postHealthData(healthData: HealthData) {
        try {
            client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(healthData)
            }
        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
        }
    }
}
