package com.example.betabuddy.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.betabuddy.model.ChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreChatRepository : ChatRepository {

    private val db = FirebaseFirestore.getInstance()

    // Use emails (meId, peerId) – order them so both sides get same thread id
    private fun threadId(me: String, peer: String): String =
        listOf(me, peer).sorted().joinToString("_")

    override fun messages(meId: String, peerId: String): LiveData<List<ChatMessage>> {
        val live = MutableLiveData<List<ChatMessage>>()
        db.collection("chats")
            .document(threadId(meId, peerId))
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snap, _ ->
                val list = snap?.toObjects(ChatMessage::class.java) ?: emptyList()
                live.value = list
            }
        return live
    }

    override suspend fun send(meId: String, peerId: String, text: String) {
        val data = hashMapOf(
            "messageId" to "",
            "senderId" to meId,
            "receiverId" to peerId,
            "message" to text,
            "timestamp" to Timestamp.now(),
            "readBy" to listOf(meId)        // 👈 sender has read it by default
        )

        db.collection("chats")
            .document(threadId(meId, peerId))
            .collection("messages")
            .add(data)
    }

    override fun markThreadRead(meId: String, peerId: String) {
        val ref = db.collection("chats")
            .document(threadId(meId, peerId))
            .collection("messages")

        // Fetch all messages and add meId to readBy if missing
        ref.get().addOnSuccessListener { snap ->
            for (doc in snap.documents) {
                val readBy = doc.get("readBy") as? List<*> ?: emptyList<Any>()
                if (!readBy.contains(meId)) {
                    doc.reference.update("readBy", FieldValue.arrayUnion(meId))
                }
            }
        }
    }
}