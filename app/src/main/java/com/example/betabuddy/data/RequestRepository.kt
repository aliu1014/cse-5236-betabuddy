//package com.example.betabuddy.data
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import com.example.betabuddy.model.FriendRequest
//import com.google.firebase.firestore.FirebaseFirestore
//
//class RequestRepository {
//    private val db = FirebaseFirestore.getInstance()
//    private val _requests = MutableLiveData<List<FriendRequest>>()
//    val requests: LiveData<List<FriendRequest>> get() = _requests
//
//    fun loadRequests(forUserId: String) {
//        db.collection("requests")
//            .whereEqualTo("toUserId", forUserId)
//            .addSnapshotListener { snapshot, _ ->
//                val requestList = snapshot?.toObjects(FriendRequest::class.java) ?: emptyList()
//                _requests.value = requestList
//            }
//    }
//
//    fun acceptRequest(request: FriendRequest) {
//        db.collection("requests").document(request.requestId)
//            .update("status", "accepted")
//    }
//
//    fun declineRequest(request: FriendRequest) {
//        db.collection("requests").document(request.requestId)
//            .update("status", "declined")
//    }
//}