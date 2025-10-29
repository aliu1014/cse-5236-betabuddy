package com.example.betabuddy.chat

import androidx.lifecycle.*
import com.example.betabuddy.data.ChatRepository
import com.example.betabuddy.model.ChatMessage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch
import androidx.lifecycle.switchMap

/**
 * Presents a chat thread between `me` and `peer`.
 * - Exposes LiveData of ChatMessage
 * - Also exposes mapped "You: ..." strings for your current ListView
 */
class ChatViewModel(
    private val repo: ChatRepository
) : ViewModel() {

    private val participants = MutableLiveData<Pair<String, String>>() // (meUid, peerUid)
    private var meUidCache: String = ""

    /** Live Firestore-backed messages for (me, peer) */
    val messages: LiveData<List<ChatMessage>> =
        participants.switchMap { (me, peer) ->
            meUidCache = me
            repo.messages(me, peer)
        }

    /** Convenience for your current ListView adapter */
    val messageRows: LiveData<List<String>> = messages.map { list ->
        list.map { m ->
            if (m.senderId == meUidCache) "You: ${m.message}" else "Them: ${m.message}"
        }
    }

    /** Bind this VM to a specific conversation */
    fun bind(meUid: String, peerUid: String) {
        if (participants.value != meUid to peerUid) {
            participants.value = meUid to peerUid
        }
    }

    /** Send a message in the bound thread */
    fun send(text: String) {
        val (me, peer) = participants.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch { repo.send(me, peer, text) }
    }
}

/** Factory so the Fragment can pass in the repository (from ServiceLocator or Hilt) */
class ChatVMFactory(
    private val repo: ChatRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ChatViewModel::class.java))
        return ChatViewModel(repo) as T
    }
}
