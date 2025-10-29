package com.example.betabuddy.data

import androidx.lifecycle.LiveData
import com.example.betabuddy.model.UserProfile

/**
 * UsersRepository
 * ----------------
 * Defines how to access and search for user profiles.
 * Exposes LiveData so the UI can observe results.
 */
interface UsersRepository {
    /** Current search results (observable list of users). */
    val results: LiveData<List<UserProfile>>

    /**
     * Performs a search for users, optionally filtered by location.
     * Passing null or empty string returns all users.
     */
    suspend fun search(locationEquals: String?)
}
