package com.example.betabuddy.friends

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.betabuddy.model.FriendRequest
import com.example.betabuddy.profile.User
import com.google.firebase.firestore.FieldValue
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
    fun clear() { usersReg?.remove(); usersReg = null }

    private fun updateResults(list: List<UserHit>) {
        _hits.value = list
        _resultRows.value = list.map { hit ->
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
                        !u.location.equals(target, ignoreCase = true)) return@mapNotNull null
                    UserHit(email, u)
                } ?: emptyList()

                _hits.value = list
                _resultRows.value = list.map { hit ->
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
                        // Remove that user from the current search results
                        val curr = _hits.value?.toMutableList() ?: mutableListOf()
                        val idx = curr.indexOfFirst { it.email == toEmail }
                        if (idx >= 0) {
                            curr.removeAt(idx)
                            _hits.value = curr
                            _resultRows.value = curr.map { hit ->
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
                    }
            }
    }
}

