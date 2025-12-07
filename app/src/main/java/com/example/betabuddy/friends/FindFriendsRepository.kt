package com.example.betabuddy.friends

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.betabuddy.model.FriendRequest
import com.example.betabuddy.profile.User
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import android.location.Location

class FindFriendsRepository {

    /** One search hit with the document id (email) + the user payload */
    data class UserHit(val email: String, val user: User)

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var usersReg: ListenerRegistration? = null

    // Full objects used by the Fragment to get the email/docId at a row index
    private val _hits = MutableLiveData<List<UserHit>>(emptyList())
    val hits: MutableLiveData<List<UserHit>> get() = _hits

    // Pretty strings for your existing SimpleResultsAdapter
    private val _resultRows = MutableLiveData<List<String>>(emptyList())
    val resultRows: MutableLiveData<List<String>> get() = _resultRows

    // Emails to exclude from search results
    private var friendEmails: Set<String> = emptySet()
    private var pendingRequestEmails: Set<String> = emptySet() // anyone with a pending req (in or out)

    fun clear() {
        usersReg?.remove()
        usersReg = null
        // (optional) you could also store/remove the listeners created in startListeningForExclusions()
    }

    /** Apply filtering (friends + pending) and build display strings. */
    private fun updateResults(list: List<UserHit>) {
        // Filter out friends and pending requests
        val filtered = list.filter { hit ->
            val email = hit.email
            email !in friendEmails && email !in pendingRequestEmails
        }

        _hits.value = filtered
        _resultRows.value = filtered.map { hit ->
            val u = hit.user
            val name = u.name.ifBlank { u.username.ifBlank { "Anonymous" } }
            val role = when {
                u.gradeLead.isNotBlank()    -> "Lead ${u.gradeLead}"
                u.gradeTopRope.isNotBlank() -> "Top rope ${u.gradeTopRope}"
                u.gradeBoulder.isNotBlank() -> "Boulder ${u.gradeBoulder}"
                else                        -> "Climber"
            }
            val loc = u.location.ifBlank { "Anywhere" }
            "$name ($role) — $loc"
        }
    }

    /** Live search of all other users. If [location] is blank, shows everyone else. */
    fun searchUsers(location: String?) {
        val me = auth.currentUser?.email ?: return
        val target = location?.trim().orEmpty()

        usersReg?.remove()
        usersReg = db.collection("users")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    val u = doc.toObject<User>() ?: return@mapNotNull null
                    val email = doc.id                       // <-- email is the doc id
                    if (email == me) return@mapNotNull null   // exclude myself
                    if (target.isNotEmpty() &&
                        !u.location.equals(target, ignoreCase = true)
                    ) return@mapNotNull null

                    UserHit(email, u)
                } ?: emptyList()

                // IMPORTANT: let updateResults handle exclusions + pretty strings
                updateResults(list)
            }
    }

    fun searchNearbyUsers(myLat: Double, myLng: Double, radiusMiles: Double) {
        val me = auth.currentUser?.email ?: return
        val radiusMeters = radiusMiles * 1609.34

        usersReg?.remove()
        usersReg = db.collection("users")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    val u = doc.toObject<User>() ?: return@mapNotNull null
                    val email = doc.id
                    if (email == me) return@mapNotNull null      // exclude myself

                    val uLat = u.latitude
                    val uLng = u.longitude
                    if (uLat == null || uLng == null) return@mapNotNull null

                    val result = FloatArray(1)
                    Location.distanceBetween(
                        myLat, myLng,
                        uLat, uLng,
                        result
                    )
                    val distanceMeters = result[0]
                    if (distanceMeters > radiusMeters) return@mapNotNull null

                    UserHit(email, u)
                } ?: emptyList()

                updateResults(list)
            }
    }

    /**
     * Send a friend request to [toEmail], then remove that user from current results.
     * Writes to: friendRequests/{toEmail}/incoming/{myEmail}
     */
    fun sendFriendRequest(toEmail: String, message: String = "") {
        val meEmail = auth.currentUser?.email ?: return

        // Load my profile so recipient sees a name; optional but nicer
        db.collection("users").document(meEmail).get()
            .addOnSuccessListener { meDoc ->
                val me = meDoc.toObject<User>()
                val senderName = when {
                    !me?.name.isNullOrBlank()     -> me!!.name
                    !me?.username.isNullOrBlank() -> me!!.username
                    else                           -> ""
                }

                val req = FriendRequest(
                    senderEmail = meEmail,
                    senderName = senderName,
                    recipientEmail = toEmail,
                    message = message,
                    createdAt = System.currentTimeMillis()
                )

                db.collection("friendRequests")
                    .document(toEmail)
                    .collection("incoming")
                    .document(meEmail)
                    .set(req)
                    .addOnSuccessListener {
                        // Mark it as pending locally and re-filter
                        pendingRequestEmails = pendingRequestEmails + toEmail

                        // Remove that user from the current search results immediately
                        val curr = _hits.value?.toMutableList() ?: mutableListOf()
                        val idx = curr.indexOfFirst { it.email == toEmail }
                        if (idx >= 0) {
                            curr.removeAt(idx)
                            updateResults(curr)   // <-- use shared logic
                        }
                    }
            }
    }

    fun searchNearbyFromMyProfile(radiusMiles: Double) {
        val meEmail = auth.currentUser?.email ?: return

        db.collection("users").document(meEmail).get()
            .addOnSuccessListener { doc ->
                val me = doc.toObject<User>() ?: return@addOnSuccessListener
                val myLat = me.latitude
                val myLng = me.longitude

                if (myLat == null || myLng == null) {
                    // No coordinates saved for me; fall back to city search or show all
                    searchUsers(null)
                    return@addOnSuccessListener
                }

                // Re-use your existing nearby logic
                searchNearbyUsers(myLat, myLng, radiusMiles)
            }
    }

    /** Listen to my friends + requests to build exclusion sets. */
    fun startListeningForExclusions() {
        val me = auth.currentUser?.email ?: return

        // 1) Friends list
        db.collection("friends")
            .document(me)
            .collection("friendList")
            .addSnapshotListener { snap, _ ->
                friendEmails = snap?.documents
                    ?.map { it.id }       // friend email as doc id
                    ?.toSet()
                    ?: emptySet()

                // Re-filter current results when friends change
                _hits.value?.let { updateResults(it) }
            }

        // 2) Requests involving me
        // Incoming requests TO me
        db.collection("friendRequests")
            .document(me)
            .collection("incoming")
            .addSnapshotListener { snap, _ ->
                val incoming = snap?.documents
                    ?.mapNotNull { it.getString("senderEmail") }
                    ?.toSet()
                    ?: emptySet()

                // Outgoing requests I sent (senderEmail == me)
                db.collectionGroup("incoming")
                    .whereEqualTo("senderEmail", me)
                    .addSnapshotListener { outSnap, _ ->
                        val outgoing = outSnap?.documents
                            ?.mapNotNull { it.getString("recipientEmail") }
                            ?.toSet()
                            ?: emptySet()

                        pendingRequestEmails = incoming + outgoing

                        // Re-filter current results
                        _hits.value?.let { updateResults(it) }
                    }
            }
    }
}