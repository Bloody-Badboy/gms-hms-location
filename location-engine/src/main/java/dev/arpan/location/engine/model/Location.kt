package dev.arpan.location.engine.model

import android.location.Location

data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val bearing: Float,
    val speed: Float,
    val fromMockProvider: Boolean,
    val timestamp: Long
) {
    companion object {
        fun fromLocation(location: Location) = Location(
            location.latitude,
            location.longitude,
            location.accuracy,
            location.bearing,
            location.speed,
            location.isFromMockProvider,
            location.time
        )
    }
}
