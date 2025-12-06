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
import com.google.firebase.auth.FirebaseAuth // ⭐ NEW
import com.example.betabuddy.data.FirestoreChatRepository // ⭐ NEW
/**
 * ChatFragment
 * ------------
 * This fragment handles the chat interface within the BetaBuddy app
 * It allows users to: send/view chat messages displayed in a ListView
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

    //Where UI elements are connected and event listeners are defined
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageInput = view.findViewById(R.id.etMessage)
        sendButton = view.findViewById(R.id.btnSend)
        chatListView = view.findViewById(R.id.lvChat)
        //Set up a simple ArrayAdapter to display messages in the ListView
        chatAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, chatMessages)
        chatListView.adapter = chatAdapter

        // Set up the Back button to navigate back to the FindFriendsFragment
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

        // Observe Firestore messages
//        viewModel.messageRows.observe(viewLifecycleOwner) { rows ->
//            chatMessages.clear()
//            chatMessages.addAll(rows)
//            chatAdapter.notifyDataSetChanged()
//            chatListView.smoothScrollToPosition(chatMessages.size - 1)
//
//            // Mark them as read immediately
//            viewModel.markThreadRead()
//        }
        viewModel.messageRows.observe(viewLifecycleOwner) { rows ->
            // Keep only the last 100 messages in memory
            val limited = if (rows.size > 100) rows.takeLast(100) else rows

            chatMessages.clear()
            chatMessages.addAll(limited)

            chatAdapter.notifyDataSetChanged()
            chatListView.smoothScrollToPosition(chatMessages.size - 1)

            viewModel.markThreadRead()
        }

        // Send a Firestore message, not just local UI
        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.send(message)
                messageInput.text.clear()
            }
        }
    }
}
