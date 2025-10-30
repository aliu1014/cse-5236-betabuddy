package com.example.betabuddy.profile

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.toObject

/**
 * Handles all Firestore CRUD operations for user profiles.
 * Decouples Firestore logic from ViewModel and UI layers.
 */
class ProfileRepository {

    private val db = Firebase.firestore

    // Create or update user profile
    fun saveUser(user: User, onResult: (Boolean) -> Unit) {
        db.collection("users")
            .document(user.username)
            .set(user)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    // Get user profile by username
    fun getUser(username: String, onResult: (User?) -> Unit) {
        db.collection("users")
            .document(username)
            .get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.toObject<User>()
                onResult(user)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    // Delete user profile
    fun deleteUser(username: String, onResult: (Boolean) -> Unit) {
        db.collection("users")
            .document(username)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    // See if username already taken
    fun usernameExists(username: String, onResult: (Boolean) -> Unit) {
        db.collection("users")
            .document(username)
            .get()

            // true if username already exists
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.exists())
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}