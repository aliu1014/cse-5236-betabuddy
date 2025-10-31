package com.example.betabuddy.request

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.betabuddy.model.FriendRequest

class RequestsViewModel(
    private val repo: RequestsRepository = RequestsRepository()
) : ViewModel() {

    val requests: LiveData<List<FriendRequest>> = repo.requests

    fun start() = repo.listenIncomingRequests()
    fun stop()  = repo.stopListening()

    fun accept(req: FriendRequest, onResult: (Boolean) -> Unit = {}) =
        repo.acceptRequest(req, onResult)

    fun decline(req: FriendRequest, onResult: (Boolean) -> Unit = {}) =
        repo.declineRequest(req, onResult)

    override fun onCleared() {
        super.onCleared()
        repo.stopListening()
    }
}


