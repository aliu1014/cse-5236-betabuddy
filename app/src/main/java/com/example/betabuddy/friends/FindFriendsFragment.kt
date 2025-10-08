package com.example.betabuddy.find

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import com.example.betabuddy.friendlist.FriendsFragment
import com.example.betabuddy.profile.ProfileFragment
import com.example.betabuddy.requests.RequestsFragment

class FindFriendsFragment : BaseLoggingFragment(R.layout.fragment_find_friends) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val et = view.findViewById<EditText>(R.id.etFilterLocation)
        val rv = view.findViewById<RecyclerView>(R.id.rvResults)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = SimpleResultsAdapter(
            onViewInfo = {
                parentFragmentManager.commit {
                    replace(R.id.fragment_container_view, ProfileFragment()) // detail screen later
                    addToBackStack(null)
                }
            },
            onSendRequest = { /* no-op placeholder for now */ }
        )

        view.findViewById<Button>(R.id.btnSearch).setOnClickListener {
            // For now we just populate mock rows
            (rv.adapter as SimpleResultsAdapter).submit(
                listOf(
                    "Alex (Lead 5.10b) — ${et.text.ifBlank { "Anywhere" }}",
                    "Priya (Top rope 5.9) — ${et.text.ifBlank { "Anywhere" }}",
                    "Sam (Boulder V4) — ${et.text.ifBlank { "Anywhere" }}"
                )
            )
        }
        view.findViewById<Button>(R.id.btnPending).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, RequestsFragment())
                addToBackStack(null)
            }
        }
        view.findViewById<Button>(R.id.btnViewFriends).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, FriendsFragment())
                addToBackStack(null)
            }
        }
    }
}

/** Minimal adapter for mock results (no Firebase). */
private class SimpleResultsAdapter(
    val onViewInfo: () -> Unit,
    val onSendRequest: () -> Unit
) : RecyclerView.Adapter<TextRowVH>() {

    private val data = mutableListOf<String>()
    fun submit(items: List<String>) { data.clear(); data.addAll(items); notifyDataSetChanged() }

    override fun onCreateViewHolder(p: android.view.ViewGroup, vType: Int): TextRowVH {
        val v = android.view.LayoutInflater.from(p.context)
            .inflate(R.layout.row_find_result, p, false)
        return TextRowVH(v, onViewInfo, onSendRequest)
    }
    override fun onBindViewHolder(h: TextRowVH, pos: Int) = h.bind(data[pos])
    override fun getItemCount() = data.size
}

private class TextRowVH(
    itemView: android.view.View,
    val onViewInfo: () -> Unit,
    val onSendRequest: () -> Unit
) : RecyclerView.ViewHolder(itemView) {
    fun bind(text: String) {
        itemView.findViewById<android.widget.TextView>(R.id.tvName).text = text
        itemView.findViewById<android.widget.TextView>(R.id.tvLocation).text = ""
        itemView.findViewById<android.widget.Button>(R.id.btnViewInfo).setOnClickListener { onViewInfo() }
        itemView.findViewById<android.widget.Button>(R.id.btnRequest).setOnClickListener { onSendRequest() }
    }
}

