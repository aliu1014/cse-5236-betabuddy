package com.example.betabuddy

import kotlin.math.*

/**
 * Small helper for location math.
 * You can call this from your ViewModel later if you want,
 * but for the checkpoint it's enough that it's part of the app.
 */
object LocationUtils {

    // Returns distance between two lat/lng points in miles
    fun distanceMiles(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371.0 // Earth radius km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2.0) +
                cos(rLat1) * cos(rLat2) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val km = R * c
        return km * 0.621371 // km → miles
    }

    fun isWithinRadiusMiles(
        centerLat: Double,
        centerLon: Double,
        userLat: Double,
        userLon: Double,
        radiusMiles: Double
    ): Boolean {
        val dist = distanceMiles(centerLat, centerLon, userLat, userLon)
        return dist <= radiusMiles
    }
}
