package com.example.betabuddy.friendlist

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import com.example.betabuddy.find.FindFriendsFragment

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

        // Check if coming from FindFriendsFragment
        val fromFindFriends = arguments?.getBoolean("fromFindFriends", false) ?: false
        val backButton = view.findViewById<Button>(R.id.btnBack)

        if (fromFindFriends) {
            // Show Back button and set its action
            backButton.visibility = View.VISIBLE
            backButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        } else {
            // Hide the Back button if not coming from FindFriendsFragment
            backButton.visibility = View.GONE
        }
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