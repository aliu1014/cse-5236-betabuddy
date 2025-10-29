package com.example.betabuddy.data
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.betabuddy.model.Friend
import com.google.firebase.firestore.FirebaseFirestore

class FriendRepository {
    private val db = FirebaseFirestore.getInstance()
    private val _friends = MutableLiveData<List<Friend>>()
    val friends: LiveData<List<Friend>> get() = _friends

    fun loadFriends(userId: String) {
        db.collection("friends")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                val friendList = snapshot?.toObjects(Friend::class.java) ?: emptyList()
                _friends.value = friendList
            }
    }
}