package com.example.betabuddy.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    private val repository = ProfileRepository()

    // LiveData for observing a user
    val user = MutableLiveData<User?>()

    fun loadUser() {
        repository.getUser { loadedUser ->
            user.value = loadedUser
        }
    }

    fun saveUser(user: User, onResult: (Boolean) -> Unit) {
        repository.saveUser(user, onResult)
    }

    fun deleteUser(username: String, onResult: (Boolean) -> Unit) {
        repository.deleteUser(username, onResult)
    }

    fun usernameExists(username: String, onResult: (Boolean) -> Unit) {
        repository.usernameExists(username, onResult)
    }
}