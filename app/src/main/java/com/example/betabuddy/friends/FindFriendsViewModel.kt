package com.example.betabuddy.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel for Find Friends & Pending Requests.
 */
class FindFriendsViewModel(
    private val repo: FindFriendsRepository = FindFriendsRepository()
) : ViewModel() {
    val hits: LiveData<List<FindFriendsRepository.UserHit>> = repo.hits
    val resultRows: LiveData<List<String>> = repo.resultRows
    fun search(location: String?) = repo.searchUsers(location)
    fun sendRequest(toEmail: String, message: String = "") =
        repo.sendFriendRequest(toEmail, message)
    override fun onCleared() {
        super.onCleared()
        repo.clear()
    }
}


