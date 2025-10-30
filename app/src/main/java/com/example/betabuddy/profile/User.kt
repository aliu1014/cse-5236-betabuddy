package com.example.betabuddy.profile

/**
 * Simple data model for storing user profiles in Firestore.
 * Matches the fields used in ProfileFragment and UserRepository.
 */
data class User(
    val username: String = "",
    val location: String = "",
    val gender: String = "",
    val age: Int = 0,
    val weight: Int = 0,
    val name: String = "",
    val feet: Int = 0,
    val inches: Int = 0,
    val gradeTopRope: String = "",
    val gradeBoulder: String = "",
    val gradeLead: String = "",
    val hasGear: Boolean = false,
    val hasTopRopeCert: Boolean = false,
    val hasLeadCert: Boolean = false
)
