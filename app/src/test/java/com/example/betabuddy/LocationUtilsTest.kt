package com.example.betabuddy


import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class LocationUtilsTest {

    @Test
    fun distance_isZeroForSamePoint() {
        val d = LocationUtils.distanceMiles(
            40.0, -83.0,
            40.0, -83.0
        )
        assertEquals(0.0, d, 0.0001)
    }

    @Test
    fun distance_betweenKnownCities_isReasonable() {
        // Columbus, OH to Cincinnati, OH (roughly ~100 miles)
        val d = LocationUtils.distanceMiles(
            39.9612, -82.9988,   // Columbus
            39.1031, -84.5120    // Cincinnati
        )
        // Check it's between 90 and 120 miles
        assertTrue(d > 90)
        assertTrue(d < 120)
    }

    @Test
    fun isWithinRadiusMiles_returnsTrueAndFalseCorrectly() {
        val centerLat = 39.9612
        val centerLon = -82.9988     // Columbus
        val nearLat   = 40.0
        val nearLon   = -83.0
        val farLat    = 41.8781
        val farLon    = -87.6298     // Chicago

        assertTrue(
            LocationUtils.isWithinRadiusMiles(
                centerLat, centerLon, nearLat, nearLon, radiusMiles = 50.0
            )
        )

        assertFalse(
            LocationUtils.isWithinRadiusMiles(
                centerLat, centerLon, farLat, farLon, radiusMiles = 50.0
            )
        )
    }
}
