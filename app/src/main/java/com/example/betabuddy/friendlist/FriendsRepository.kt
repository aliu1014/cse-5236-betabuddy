package com.example.betabuddy.friendlist

import androidx.lifecycle.MutableLiveData
import com.example.betabuddy.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


// Carry both the Firestore doc id (key) and the stored profile
data class FriendEdge(
    val key: String,
    val profile: UserProfile,
    val unread: Int = 0
)
class FriendsRepository {

    private val db = FirebaseFirestore.getInstance()
    val friends = MutableLiveData<List<FriendEdge>>()

    fun loadFriends(onResult: (Boolean) -> Unit) {
        val me = FirebaseAuth.getInstance().currentUser?.email ?: return
        db.collection("friends").document(me).collection("friendList")
            .get()
            .addOnSuccessListener { snap ->
                friends.value = snap.documents.mapNotNull { d ->
                    d.toObject(UserProfile::class.java)?.let { FriendEdge(d.id, it) }
                }
                onResult(true)
            }
            .addOnFailureListener { onResult(false) }
    }

    /** Remove friend in BOTH directions: me→them and them→me */
    fun removeFriendBothWays(friendKey: String, onResult: (Boolean) -> Unit) {
        val me = FirebaseAuth.getInstance().currentUser?.email ?: return

        val myDoc   = db.collection("friends").document(me)
            .collection("friendList").document(friendKey)
        val theirDoc = db.collection("friends").document(friendKey)
            .collection("friendList").document(me)

        db.runBatch { b ->
            b.delete(myDoc)
            b.delete(theirDoc)
        }
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}

