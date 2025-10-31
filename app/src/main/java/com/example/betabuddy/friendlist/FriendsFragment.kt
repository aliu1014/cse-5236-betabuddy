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
import com.example.betabuddy.model.UserProfile

/**
 * Displays a live list of friends from Firestore using FriendsViewModel.
 * Each friend can be tapped to open a chat or removed.
 */
class FriendsFragment : BaseLoggingFragment(R.layout.fragment_friends) {

    private val viewModel: FriendsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvFriends)
        val backButton = view.findViewById<Button>(R.id.btnBack)

        rv.layoutManager = LinearLayoutManager(requireContext())

        // Adapter handles both chat + remove
        val adapter = FriendsAdapter(
            onChatClick = { friend ->
                val chat = ChatFragment().apply {
                    arguments = Bundle().apply {
                        putString("chatPartnerName", friend.name.ifBlank { friend.email })
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, chat)
                    .addToBackStack(null)
                    .commit()
            },
            onRemoveClick = { friend ->
                viewModel.removeFriend(friend.email)
                Toast.makeText(requireContext(), "Removed ${friend.name}", Toast.LENGTH_SHORT).show()
                viewModel.loadFriends()
            }
        )
        rv.adapter = adapter

        // Observe full list of friends (UserProfile)
        viewModel.friends.observe(viewLifecycleOwner) { list ->
            adapter.submit(list)
            if (list.isEmpty()) {
                Toast.makeText(requireContext(), "No friends yet. Add some!", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadFriends()

        val fromFindFriends = arguments?.getBoolean("fromFindFriends", false) ?: false
        backButton.visibility = if (fromFindFriends) View.VISIBLE else View.GONE
        backButton.setOnClickListener { parentFragmentManager.popBackStack() }
    }
}

/**
 * Adapter for showing friends with chat and remove buttons.
 */
private class FriendsAdapter(
    private val onChatClick: (UserProfile) -> Unit,
    private val onRemoveClick: (UserProfile) -> Unit
) : RecyclerView.Adapter<FriendVH>() {

    private val data = mutableListOf<UserProfile>()

    fun submit(items: List<UserProfile>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): FriendVH {
        val v = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.row_friend, parent, false)
        return FriendVH(v, onChatClick, onRemoveClick)
    }

    override fun onBindViewHolder(holder: FriendVH, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}

/**
 * ViewHolder for a single friend row (name + chat + remove button)
 */
private class FriendVH(
    itemView: android.view.View,
    private val onChatClick: (UserProfile) -> Unit,
    private val onRemoveClick: (UserProfile) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val nameView = itemView.findViewById<android.widget.TextView>(R.id.tvFriendName)
    private val chatBtn = itemView.findViewById<android.widget.ImageButton>(R.id.btnRowChat)
    private val removeBtn = itemView.findViewById<android.widget.Button>(R.id.btnRowRemove)

    fun bind(friend: UserProfile) {
        nameView.text = friend.name.ifBlank { friend.email }
        chatBtn.setOnClickListener { onChatClick(friend) }
        removeBtn.setOnClickListener { onRemoveClick(friend) }
    }
}