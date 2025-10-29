package com.example.betabuddy.model

data class Friend(
    val userId: String = "",
    val friendId: String = "",
    val since: Long = System.currentTimeMillis()
)