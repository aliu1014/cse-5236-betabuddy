package com.example.betabuddy.friendlist

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment

class FriendsFragment : BaseLoggingFragment(R.layout.fragment_friends) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rvFriends)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = SimpleTextAdapter(listOf("User #1", "User #2", "User #3"))
    }
}

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
private class TextVH(item: android.view.View) : RecyclerView.ViewHolder(item) {
    fun bind(text: String) {
        itemView.findViewById<android.widget.TextView>(R.id.tvFriendName).text = text
    }
}