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

        val adapter = FriendsAdapter(
            onChatClick = { edge ->
                val chat = ChatFragment().apply {
                    arguments = Bundle().apply {
                        putString("chatPartnerName", edge.profile.name.ifBlank { edge.key })
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, chat)
                    .addToBackStack(null)
                    .commit()
            },
            onRemoveClick = { edge ->
                // Optimistic UI: remove immediately
                val current = viewModel.friends.value ?: emptyList()
                val newList = current.filterNot { it.key == edge.key }
                (rv.adapter as FriendsAdapter).submit(newList)
                viewModel.removeFriendByKey(edge.key)
                // Optionally refresh from server after
                viewModel.loadFriends()
            }
        )
        rv.adapter = adapter

        viewModel.friends.observe(viewLifecycleOwner) { edges ->
            adapter.submit(edges)
            if (edges.isEmpty()) {
                Toast.makeText(requireContext(), "No friends yet. Add some!", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadFriends()

        val fromFindFriends = arguments?.getBoolean("fromFindFriends", false) ?: false
        backButton.visibility = if (fromFindFriends) View.VISIBLE else View.GONE
        backButton.setOnClickListener { parentFragmentManager.popBackStack() }
    }
}

private class FriendsAdapter(
    private val onChatClick: (FriendEdge) -> Unit,
    private val onRemoveClick: (FriendEdge) -> Unit
) : RecyclerView.Adapter<FriendVH>() {

    private val data = mutableListOf<FriendEdge>()
    fun submit(items: List<FriendEdge>) { data.clear(); data.addAll(items); notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): FriendVH {
        val v = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.row_friend, parent, false)
        return FriendVH(v, onChatClick, onRemoveClick)
    }
    override fun onBindViewHolder(h: FriendVH, pos: Int) = h.bind(data[pos])
    override fun getItemCount() = data.size
}

private class FriendVH(
    itemView: android.view.View,
    val onChatClick: (FriendEdge) -> Unit,
    val onRemoveClick: (FriendEdge) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val nameView = itemView.findViewById<android.widget.TextView>(R.id.tvFriendName)
    private val chatBtn  = itemView.findViewById<android.widget.ImageButton>(R.id.btnRowChat)
    private val removeBtn = itemView.findViewById<android.widget.Button>(R.id.btnRowRemove)

    fun bind(edge: FriendEdge) {
        nameView.text = edge.profile.name.ifBlank { edge.key }
        chatBtn.setOnClickListener { onChatClick(edge) }
        removeBtn.setOnClickListener { onRemoveClick(edge) }
    }
}
