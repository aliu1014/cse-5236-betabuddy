package com.example.betabuddy.profile

import com.google.firebase.auth.FirebaseAuth
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
        val email = FirebaseAuth.getInstance().currentUser?.email ?: return
        db.collection("users").document(email)
            .set(user)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    // Get user profile by username
    fun getUser(username: String, onResult: (User?) -> Unit) {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: return
        db.collection("users").document(email)
            .get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.toObject(User::class.java)
                onResult(user)
            }
            .addOnFailureListener { onResult(null) }
    }

    // Delete user profile
    fun deleteUser(username: String, onResult: (Boolean) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: return

        db.collection("users").document(email).delete()
            .addOnSuccessListener {
                auth.currentUser?.delete()
                    ?.addOnSuccessListener { onResult(true) }
                    ?.addOnFailureListener { onResult(false) }
            }
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