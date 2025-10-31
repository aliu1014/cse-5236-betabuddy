package com.example.betabuddy.friendlist

import androidx.lifecycle.MutableLiveData
import com.example.betabuddy.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

//class FriendsRepository {
//
//    private val db = FirebaseFirestore.getInstance()
//    val friends = MutableLiveData<List<UserProfile>>()
//
//    /** Load the current user's friends list from Firestore */
//    fun loadFriends(onResult: (Boolean) -> Unit) {
//        val email = FirebaseAuth.getInstance().currentUser?.email ?: return
//
//        db.collection("friends")
//            .document(email)
//            .collection("friendList")
//            .get()
//            .addOnSuccessListener { snapshot ->
//                val list = snapshot.documents.mapNotNull { it.toObject(UserProfile::class.java) }
//                friends.value = list
//                onResult(true)
//            }
//            .addOnFailureListener {
//                onResult(false)
//            }
//    }
//
//    /** Add a friend to the current user's friend list */
//    fun addFriend(friend: UserProfile, onResult: (Boolean) -> Unit) {
//        val email = FirebaseAuth.getInstance().currentUser?.email ?: return
//
//        db.collection("friends")
//            .document(email)
//            .collection("friendList")
//            .document(friend.email) // friend’s email as key
//            .set(friend)
//            .addOnSuccessListener { onResult(true) }
//            .addOnFailureListener { onResult(false) }
//    }
//
//    /** Remove a friend from the current user's list */
//    fun removeFriend(friendEmail: String, onResult: (Boolean) -> Unit) {
//        val email = FirebaseAuth.getInstance().currentUser?.email ?: return
//
//        db.collection("friends")
//            .document(email)
//            .collection("friendList")
//            .document(friendEmail)
//            .delete()
//            .addOnSuccessListener { onResult(true) }
//            .addOnFailureListener { onResult(false) }
//    }
//}

// Carry both the Firestore doc id (key) and the stored profile
data class FriendEdge(val key: String, val profile: UserProfile)

class FriendsRepository {
    private val db = FirebaseFirestore.getInstance()
    val friends = MutableLiveData<List<FriendEdge>>()

    fun loadFriends(onResult: (Boolean) -> Unit) {
        val me = FirebaseAuth.getInstance().currentUser?.email ?: return
        db.collection("friends").document(me).collection("friendList")
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { doc ->
                    doc.toObject(UserProfile::class.java)?.let { FriendEdge(doc.id, it) }
                }
                friends.value = list
                onResult(true)
            }
            .addOnFailureListener { onResult(false) }
    }

    fun removeFriend(key: String, onResult: (Boolean) -> Unit) {
        val me = FirebaseAuth.getInstance().currentUser?.email ?: return
        db.collection("friends").document(me).collection("friendList")
            .document(key)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}

