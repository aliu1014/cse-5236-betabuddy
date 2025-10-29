package com.example.betabuddy.profile

import androidx.lifecycle.LiveData
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
}
