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

/**
 * FindFriendsFragment
 * --------------------
 * This fragment allows users to search for new climbing partners based on location and preferences.
 * The UI contains:
 *  - A text input for filtering by location
 *  - Buttons for "Search", "Pending Requests", and "View Friends"
 *  - A RecyclerView showing mock search results
 */
class FindFriendsFragment : BaseLoggingFragment(R.layout.fragment_find_friends) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get reference to the location filter input
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

        // Navigate to RequestsFragment when "Pending Requests" is clicked
        view.findViewById<Button>(R.id.btnPending).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, RequestsFragment())
                addToBackStack(null)
            }
        }

        // Navigate to FriendsFragment when "View Friends" is clicked
        view.findViewById<Button>(R.id.btnViewFriends).setOnClickListener {
            val fragment = FriendsFragment()
            val args = Bundle().apply {
                putBoolean("fromFindFriends", true)
            }
            fragment.arguments = args

            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, fragment)
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

    // Internal list of data items (friend search results)
    private val data = mutableListOf<String>()

    // Replaces current list items and refreshes the RV

    fun submit(items: List<String>) { data.clear(); data.addAll(items); notifyDataSetChanged() }

    // Inflates the row layout for each search result
    override fun onCreateViewHolder(p: android.view.ViewGroup, vType: Int): TextRowVH {
        val v = android.view.LayoutInflater.from(p.context)
            .inflate(R.layout.row_find_result, p, false)
        return TextRowVH(v, onViewInfo, onSendRequest)
    }

    // Binds each search result (text) to a row
    override fun onBindViewHolder(h: TextRowVH, pos: Int) = h.bind(data[pos])
    override fun getItemCount() = data.size
}

// Represents one row (one search result) in the RV where each row has name, location, and buttons for "ViewInfo" and "Send Request"
private class TextRowVH(
    itemView: android.view.View,
    val onViewInfo: () -> Unit,
    val onSendRequest: () -> Unit
) : RecyclerView.ViewHolder(itemView) {

    // Binds the text data and attaches click listeners for each row
    fun bind(text: String) {
        itemView.findViewById<android.widget.TextView>(R.id.tvName).text = text
        itemView.findViewById<android.widget.TextView>(R.id.tvLocation).text = ""
//        itemView.findViewById<android.widget.Button>(R.id.btnViewInfo).setOnClickListener { onViewInfo() }
        itemView.findViewById<android.widget.Button>(R.id.btnRequest).setOnClickListener { onSendRequest() }
    }
}

