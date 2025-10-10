package com.example.betabuddy.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import com.example.betabuddy.friendlist.FriendsFragment

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

         //Send button click listener to perform chat actions
        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                chatMessages.add("You: $message")
                chatAdapter.notifyDataSetChanged()
                messageInput.text.clear()
                chatListView.smoothScrollToPosition(chatMessages.size - 1)
            }
        }
    }
}
