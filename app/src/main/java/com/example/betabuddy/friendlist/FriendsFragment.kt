package com.example.betabuddy.friendlist

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment

/**
 * This fragment displays a simple list of friends using a RecyclerView.
 * It extends BaseLoggingFragment to automatically log lifecycle events
 * and include the Home button functionality.
 */
class FriendsFragment : BaseLoggingFragment(R.layout.fragment_friends) {
    // Called immediately after the fragment's view has been created. Initialize the RecyclerView with a layout manager and an adapter.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rvFriends)

        //Display items in vertical list
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = SimpleTextAdapter(listOf("User #1", "User #2", "User #3"))
    }
}

// Display lists of strings where each is bound to a TextView inside a row layout
private class SimpleTextAdapter(private val items: List<String>) :
    RecyclerView.Adapter<TextVH>() {
    override fun onCreateViewHolder(p: android.view.ViewGroup, v: Int): TextVH {
        val v = android.view.LayoutInflater.from(p.context)
            .inflate(R.layout.row_friend, p, false)
        return TextVH(v)
    }
    override fun onBindViewHolder(h: TextVH, i: Int) = h.bind(items[i])
    override fun getItemCount() = items.size
}

// Represents one row or item in RV. Each row shows name of one friend using a TextView
private class TextVH(item: android.view.View) : RecyclerView.ViewHolder(item) {
    fun bind(text: String) {
        itemView.findViewById<android.widget.TextView>(R.id.tvFriendName).text = text
    }
}