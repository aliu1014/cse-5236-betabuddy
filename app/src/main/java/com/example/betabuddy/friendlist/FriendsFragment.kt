package com.example.betabuddy.friendlist

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.betabuddy.R
import com.example.betabuddy.chat.ChatFragment
import com.example.betabuddy.core.BaseLoggingFragment

/**
 * Displays a live list of friends from Firestore using FriendsViewModel.
 * Each friend can be tapped to open a chat.
 */
class FriendsFragment : BaseLoggingFragment(R.layout.fragment_friends) {

    private val viewModel: FriendsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvFriends)
        val backButton = view.findViewById<Button>(R.id.btnBack)

        // Set up RecyclerView
        rv.layoutManager = LinearLayoutManager(requireContext())

        // Observe LiveData from ViewModel and update RecyclerView dynamically
        viewModel.friendNames.observe(viewLifecycleOwner) { friendNames ->
            rv.adapter = FriendsAdapter(friendNames) { friendName ->
                // Navigate to ChatFragment when a friend is tapped
                val chat = ChatFragment().apply {
                    arguments = Bundle().apply {
                        putString("chatPartnerName", friendName)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, chat)
                    .addToBackStack(null)
                    .commit()
            }
        }

        // Load friends from Firestore
        viewModel.loadFriends()

        // Handle "from FindFriends" navigation
        val fromFindFriends = arguments?.getBoolean("fromFindFriends", false) ?: false
        if (fromFindFriends) {
            backButton.visibility = View.VISIBLE
            backButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        } else {
            backButton.visibility = View.GONE
        }

        // Optional toast confirmation
        viewModel.friends.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                Toast.makeText(requireContext(), "No friends yet. Add some!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/**
 * Adapter for showing friend names and chat buttons in RecyclerView.
 */
private class FriendsAdapter(
    private val items: List<String>,
    private val onChatClick: (String) -> Unit
) : RecyclerView.Adapter<FriendVH>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): FriendVH {
        val v = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.row_friend, parent, false)
        return FriendVH(v, onChatClick)
    }

    override fun onBindViewHolder(holder: FriendVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

/**
 * ViewHolder for a single friend row (name + chat button)
 */
private class FriendVH(
    itemView: android.view.View,
    private val onChatClick: (String) -> Unit
) : RecyclerView.ViewHolder(itemView) {
    private val nameView = itemView.findViewById<android.widget.TextView>(R.id.tvFriendName)
    private val chatBtn = itemView.findViewById<android.widget.ImageButton>(R.id.btnRowChat)

    fun bind(friendName: String) {
        nameView.text = friendName
        chatBtn.setOnClickListener { onChatClick(friendName) }
    }
}
