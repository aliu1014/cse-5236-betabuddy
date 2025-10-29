package com.example.betabuddy.friendlist

import androidx.lifecycle.*
import com.example.betabuddy.data.FriendsRepository
import com.example.betabuddy.model.UserProfile
import kotlinx.coroutines.launch

/**
 * FriendsViewModel
 * ----------------
 * Manages the list of the user's friends.
 * Observes Firestore (or another data source) via FriendsRepository.
 */
class FriendsViewModel(
    private val repo: FriendsRepository
) : ViewModel() {

    /** Live observable list of the user's friends */
    val friends: LiveData<List<UserProfile>> = repo.friends

    /** Simple string-mapped version for your current adapter */
    val friendNames: LiveData<List<String>> = Transformations.map(friends) { list ->
        list.map { it.name.ifBlank { "Unknown Friend" } }
    }

    /** Loads the current user's friends from Firestore */
    fun loadFriends(currentUid: String) = viewModelScope.launch {
        repo.loadFriends(currentUid)
    }
}

/** Factory so your fragment can get the ViewModel with a repository dependency */
class FriendsVMFactory(
    private val repo: FriendsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FriendsViewModel::class.java))
        return FriendsViewModel(repo) as T
    }
}
