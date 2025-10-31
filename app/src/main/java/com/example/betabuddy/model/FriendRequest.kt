package com.example.betabuddy.model

data class FriendRequest(
    val senderEmail: String = "",
    val senderName: String = "",
    val recipientEmail: String = "",
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis()
)