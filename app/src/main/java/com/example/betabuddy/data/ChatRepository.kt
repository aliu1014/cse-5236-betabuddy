package com.example.betabuddy.data
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.betabuddy.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> get() = _messages

    fun loadMessages(user1: String, user2: String) {
        db.collection("messages")
            .whereIn("senderId", listOf(user1, user2))
            .whereIn("receiverId", listOf(user1, user2))
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(ChatMessage::class.java) ?: emptyList()
                _messages.value = list
            }
    }

    fun sendMessage(message: ChatMessage) {
        db.collection("messages").add(message)
    }
}