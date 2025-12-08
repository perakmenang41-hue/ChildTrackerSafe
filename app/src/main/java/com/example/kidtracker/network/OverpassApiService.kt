package com.example.kidtracker.network

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// -----------------------------
// Store POI model
// -----------------------------
data class StorePOI(
    val name: String,
    val lat: Double,
    val lon: Double,
    val radius: Float = 50f // meters
)

// -----------------------------
// Overpass API Retrofit
// -----------------------------
interface OverpassApi {
    @GET("api/interpreter")
    suspend fun getSupermarkets(@Query("data") query: String): OverpassResponse
}

data class OverpassResponse(val elements: List<Element>)
data class Element(val lat: Double, val lon: Double, val tags: Map<String, String>?)

// -----------------------------
// Fetch stores function
// -----------------------------
suspend fun fetchNearbySupermarkets(lat: Double, lon: Double): List<StorePOI> {
    val delta = 0.001 // ~100m bounding box
    val query = """
        [out:json];
        node["shop"="supermarket"](${lat - delta},${lon - delta},${lat + delta},${lon + delta});
        out;
    """.trimIndent()

    return try {
        val api = Retrofit.Builder()
            .baseUrl("https://overpass-api.de/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OverpassApi::class.java)

        val response = api.getSupermarkets(query)
        response.elements.map {
            StorePOI(it.tags?.get("name") ?: "Supermarket", it.lat, it.lon)
        }
    } catch (e: Exception) {
        Log.e("OverpassApiService", "Overpass API error: ${e.message}")
        emptyList()
    }
}
