package com.example.betabuddy.profile

import androidx.lifecycle.LiveData
<<<<<<< HEAD
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.*
import com.example.betabuddy.data.ProfileRepository
import com.example.betabuddy.model.UserProfile
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repo: ProfileRepository
) : ViewModel() {

    // expose the LiveData the fragment will observe
    val profile = repo.profile

    fun load(uid: String) = viewModelScope.launch {
        repo.loadProfile(uid)
    }

    fun save(updated: UserProfile) = viewModelScope.launch {
        repo.saveProfile(updated)
    }
}

/** Simple factory so you can pass a repository into the ViewModel */
class ProfileVMFactory(
    private val repo: ProfileRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ProfileViewModel::class.java))
        return ProfileViewModel(repo) as T
    }
=======
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Manages profile data between Firestore and UI.
 * Uses LiveData for reactive updates to the ProfileFragment.
 */

class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

    // LiveData to expose current user profile
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    //Loads the user's profile from Firestore
    fun loadUser(uid: String) {
        repository.getUser(uid) { result ->
            _user.postValue(result)
        }
    }

    // Saves or updates the user's profile
    fun saveUser(user: User, onResult: (Boolean) -> Unit) {
        repository.saveUser(user) { ok ->
            onResult(ok)
        }
    }

    // Check if username already exists when making new account
    fun usernameExists(username: String, onResult: (Boolean) -> Unit) {
        repository.usernameExists(username, onResult)
    }

>>>>>>> c7a8b56 (Linke database and save profile details to database)
}
