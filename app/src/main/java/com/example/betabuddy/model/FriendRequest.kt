package com.example.betabuddy.model

data class FriendRequest(
    val requestId: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "pending"  // "pending", "accepted", "declined"
)