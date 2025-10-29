package com.example.betabuddy.data
import androidx.lifecycle.LiveData
import com.example.betabuddy.model.ChatMessage


interface ChatRepository {
    fun messages(meUid: String, peerUid: String): LiveData<List<ChatMessage>>
    suspend fun send(meUid: String, peerUid: String, text: String)
}