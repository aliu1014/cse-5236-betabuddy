package com.example.betabuddy.chat

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import com.example.betabuddy.friendlist.FriendsFragment
import com.google.firebase.auth.FirebaseAuth
import com.example.betabuddy.data.FirestoreChatRepository
import android.view.inputmethod.EditorInfo
import android.view.KeyEvent

/**
 * ChatFragment
 * ------------
 * This fragment handles the chat interface within the BetaBuddy app.
 * It allows users to send/view chat messages displayed in a ListView.
 */
class ChatFragment : BaseLoggingFragment(R.layout.fragment_chat) {
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatListView: ListView
    private lateinit var chatAdapter: ArrayAdapter<String>
    private val chatMessages = mutableListOf<String>()

    private val viewModel: ChatViewModel by viewModels {
        ChatVMFactory(FirestoreChatRepository())
    }

    // Where UI elements are connected and event listeners are defined
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageInput = view.findViewById(R.id.etMessage)
        sendButton = view.findViewById(R.id.btnSend)
        chatListView = view.findViewById(R.id.lvChat)

        // Set up a simple ArrayAdapter to display messages in the ListView
        chatAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, chatMessages)
        chatListView.adapter = chatAdapter

        // Back button -> FriendsFragment
        view.findViewById<Button>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, FriendsFragment())
                .addToBackStack(null)
                .commit()
        }

        // Pulling the two IDs needed for the chat
        val meUid = FirebaseAuth.getInstance().currentUser?.email ?: ""
        val peerUid = requireArguments().getString("peerUid") ?: ""
        val partnerName = requireArguments().getString("chatPartnerName") ?: peerUid

        // Tells the VM which conversation to load
        viewModel.bind(meUid, peerUid)

        // Pass name into ViewModel
        viewModel.setPeerName(partnerName)

        // Observe Firestore messages (limit to last 100)
        viewModel.messageRows.observe(viewLifecycleOwner) { rows ->
            val limited = if (rows.size > 100) rows.takeLast(100) else rows

            chatMessages.clear()
            chatMessages.addAll(limited)

            chatAdapter.notifyDataSetChanged()
            chatListView.smoothScrollToPosition(chatMessages.size - 1)

            viewModel.markThreadRead()
        }

        // Shared send logic
        fun sendCurrentMessage() {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.send(message)
                messageInput.text.clear()
            }
        }

        // Send via button
        sendButton.setOnClickListener {
            sendCurrentMessage()
        }

        // Send via keyboard Enter / IME "Send"
        messageInput.setOnEditorActionListener { _, actionId, event ->
            val isImeSend = actionId == EditorInfo.IME_ACTION_SEND
            val isEnterKey =
                actionId == EditorInfo.IME_NULL &&
                        event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                        event.action == KeyEvent.ACTION_DOWN

            if (isImeSend || isEnterKey) {
                sendCurrentMessage()
                true
            } else {
                false
            }
        }
    }
}