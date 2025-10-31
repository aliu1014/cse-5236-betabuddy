package com.example.betabuddy.friendlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.map
import kotlinx.coroutines.launch

/**
 * Manages the list of the user's friends.
 * Observes Firestore via FriendsRepository.
 */

class FriendsViewModel(
    private val repo: FriendsRepository = FriendsRepository()
) : ViewModel() {

    val friends: LiveData<List<FriendEdge>> = repo.friends

    val friendNames: LiveData<List<String>> = friends.map { edges ->
        edges.map { it.profile.name.ifBlank { it.key } }
    }

    fun loadFriends() = viewModelScope.launch { repo.loadFriends { } }

    fun removeFriendByKey(friendKey: String) = viewModelScope.launch {
        repo.removeFriendBothWays(friendKey) { /* could show a toast if needed */ }
    }
}
