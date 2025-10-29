package com.example.betabuddy.data
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.betabuddy.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore

class ProfileRepository {
    private val db = FirebaseFirestore.getInstance()
    private val _profile = MutableLiveData<UserProfile>()
    val profile: LiveData<UserProfile> get() = _profile

    fun loadProfile(uid: String) {
        db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    _profile.value = snapshot.toObject(UserProfile::class.java)
                }
            }
    }

    fun saveProfile(profile: UserProfile) {
        db.collection("users").document(profile.uid).set(profile)
    }
}