package com.example.betabuddy.request

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.betabuddy.model.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.example.betabuddy.model.UserProfile
import com.example.betabuddy.profile.User

/**
 * Inbound friend-requests repository used by RequestsFragment.
 *
 * Firestore layout:
 *  users/{email} : UserProfile
 *  friendRequests/{recipientEmail}/incoming/{senderEmail} : FriendRequest
 *  friends/{ownerEmail}/friendList/{friendEmail} : UserProfile
 */
class RequestsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listener: ListenerRegistration? = null
    private val _requests = MutableLiveData<List<FriendRequest>>()
    val requests: LiveData<List<FriendRequest>> get() = _requests

    /** Begin listening for incoming friend requests */
    fun listenIncomingRequests() {
        val email = auth.currentUser?.email ?: return
        listener?.remove()
        listener = db.collection("friendRequests")
            .document(email)
            .collection("incoming")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject<FriendRequest>() } ?: emptyList()
                _requests.value = list
            }
    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }

    /** Accept a friend request */
    fun acceptRequest(req: FriendRequest, onResult: (Boolean) -> Unit = {}) {
        val myEmail = auth.currentUser?.email ?: run { onResult(false); return }
        val usersCol = db.collection("users")

        db.runTransaction { tx ->
            val usersCol = db.collection("users")

            // ---- me ----
            val meSnap = tx.get(usersCol.document(myEmail))              // if your user docs are keyed by email
            val me: User = meSnap.toObject(User::class.java)
                ?: User(
                    username = myEmail.substringBefore("@"),
                    name = myEmail.substringBefore("@"),
                    location = "", gender = "", age = 0, weight = 0,
                    feet = 0, inches = 0,
                    gradeTopRope = "", gradeBoulder = "", gradeLead = "",
                    hasGear = false, hasTopRopeCert = false, hasLeadCert = false
                )

            // Choose the correct ID for the sender:
            // If your docs are keyed by EMAIL:
            val senderId = req.senderEmail
            // If your docs are keyed by USERNAME instead, replace the line above with:
            // val senderId = req.senderUsername

            // ---- them (sender) ----
            val themSnap = tx.get(usersCol.document(senderId))
            val them: User = themSnap.toObject(User::class.java)
                ?: User(
                    username = senderId.substringBefore("@"),
                    name = senderId.substringBefore("@"),
                    location = "", gender = "", age = 0, weight = 0,
                    feet = 0, inches = 0,
                    gradeTopRope = "", gradeBoulder = "", gradeLead = "",
                    hasGear = false, hasTopRopeCert = false, hasLeadCert = false
                )

            // ---- write both friend edges ----
            val myFriendsDoc = db.collection("friends")
                .document(myEmail)                // or .document(myUsername) if you use usernames
                .collection("friendList")
                .document(senderId)

            val theirFriendsDoc = db.collection("friends")
                .document(senderId)
                .collection("friendList")
                .document(myEmail)                // or .document(myUsername)

            tx.set(myFriendsDoc, them)
            tx.set(theirFriendsDoc, me)

            // ---- delete the pending request ----
            val reqDoc = db.collection("friendRequests")
                .document(myEmail)                // or .document(myUsername)
                .collection("incoming")
                .document(senderId)
            tx.delete(reqDoc)

            null
        }
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    /** Decline a friend request */
    fun declineRequest(req: FriendRequest, onResult: (Boolean) -> Unit = {}) {
        val myEmail = auth.currentUser?.email ?: return onResult(false)
        db.collection("friendRequests")
            .document(myEmail)
            .collection("incoming")
            .document(req.senderEmail)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
