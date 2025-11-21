package com.example.betabuddy.friendlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.betabuddy.data.ChatRepository
import com.example.betabuddy.data.FirestoreChatRepository
import com.example.betabuddy.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Manages the list of the user's friends.
 * Observes Firestore via FriendsRepository + ChatRepository.
 */
class FriendsViewModel(
    private val repo: FriendsRepository = FriendsRepository(),
    private val chatRepo: ChatRepository = FirestoreChatRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // Live list of friends (edges: friend key + profile)
    val friends: LiveData<List<FriendEdge>> = repo.friends

    // Map<friendKey, unreadCount>
    private val _unreadCounts = MutableLiveData<Map<String, Int>>(emptyMap())
    val unreadCounts: LiveData<Map<String, Int>> get() = _unreadCounts

    // Keep references to LiveData from chat repo so we do not re-attach listeners
    private val chatSources = mutableMapOf<String, LiveData<List<ChatMessage>>>()

    init {
        // Whenever the friend list changes, attach chat listeners for unread counts
        friends.observeForever { edges ->
            attachUnreadListeners(edges)
        }
    }

    private fun attachUnreadListeners(edges: List<FriendEdge>) {
        val me = auth.currentUser?.email ?: return

        edges.forEach { edge ->
            val friendKey = edge.key
            if (!chatSources.containsKey(friendKey)) {
                // Listen to messages between me and this friend
                val src = chatRepo.messages(me, friendKey)
                chatSources[friendKey] = src

                src.observeForever { msgs ->
                    val current = _unreadCounts.value?.toMutableMap() ?: mutableMapOf()
                    val count = msgs.count { msg ->
                        // Unread = sent by them, and my id is not in readBy
                        msg.senderId != me && !msg.readBy.contains(me)
                    }
                    current[friendKey] = count
                    _unreadCounts.postValue(current)
                }
            }
        }
    }

    // Combined LiveData of friend profiles plus unread counts
    val friendsWithUnread: LiveData<List<FriendEdge>> =
        MediatorLiveData<List<FriendEdge>>().apply {
            fun recompute(
                friendList: List<FriendEdge>?,
                counts: Map<String, Int>?
            ) {
                val f = friendList ?: emptyList()
                val c = counts ?: emptyMap()
                value = f.map { edge ->
                    edge.copy(unread = c[edge.key] ?: 0)
                }
            }

            addSource(friends) { f -> recompute(f, unreadCounts.value) }
            addSource(unreadCounts) { c -> recompute(friends.value, c) }
        }

    // Simple list of friend display names (no unread badge formatting here)
    val friendNames: LiveData<List<String>> = friends.map { edges ->
        edges.map { it.profile.name.ifBlank { it.key } }
    }

    fun loadFriends() = viewModelScope.launch {
        repo.loadFriends { }
    }

    fun removeFriendByKey(friendKey: String) = viewModelScope.launch {
        repo.removeFriendBothWays(friendKey) {

        }
    }
}
