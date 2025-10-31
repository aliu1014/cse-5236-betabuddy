package com.example.betabuddy.friendlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.map
import com.example.betabuddy.model.UserProfile
import kotlinx.coroutines.launch

/**
 * Manages the list of the user's friends.
 * Observes Firestore via FriendsRepository.
 */
//class FriendsViewModel(
//    private val repo: FriendsRepository = FriendsRepository()
//) : ViewModel() {
//
//    // Live list of friends from Firestore
//    val friends: LiveData<List<UserProfile>> = repo.friends
//
//    // Simple list of friend names for easy RecyclerView display (modern LiveData.map)
//    val friendNames: LiveData<List<String>> = friends.map { list ->
//        list.map { it.name.ifBlank { "Unknown Friend" } }
//    }
//
//    // Load all friends for the current logged-in user
//    fun loadFriends() = viewModelScope.launch {
//        repo.loadFriends { /* handle result if needed */ }
//    }
//
//    // Add a friend to the current user's list
//    fun addFriend(friend: UserProfile) = viewModelScope.launch {
//        repo.addFriend(friend) { /* handle result if needed */ }
//    }
//
//    // Remove a friend by email
//    fun removeFriend(email: String) = viewModelScope.launch {
//        repo.removeFriend(email) { /* handle result if needed */ }
//    }
//}

class FriendsViewModel(
    private val repo: FriendsRepository = FriendsRepository()
) : ViewModel() {

    // List of (docId, profile)
    val friends: LiveData<List<FriendEdge>> = repo.friends

    // For simple name-only displays if needed
    val friendNames: LiveData<List<String>> = friends.map { edges ->
        edges.map { it.profile.name.ifBlank { "Unknown Friend" } }
    }

    fun loadFriends() = viewModelScope.launch { repo.loadFriends { } }

    // IMPORTANT: remove by the doc ID (key), not the profile field
    fun removeFriendByKey(key: String) = viewModelScope.launch { repo.removeFriend(key) { } }
}
