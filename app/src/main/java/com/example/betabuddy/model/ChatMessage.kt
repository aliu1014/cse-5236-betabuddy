package com.example.betabuddy.model
import com.google.firebase.Timestamp

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Timestamp? = Timestamp.now(),
    val readBy: List<String> = emptyList()
)
