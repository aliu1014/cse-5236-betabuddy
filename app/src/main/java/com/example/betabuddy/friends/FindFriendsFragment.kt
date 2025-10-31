package com.example.betabuddy.friends

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import com.example.betabuddy.friendlist.FriendsFragment
import com.example.betabuddy.profile.ProfileFragment
import com.example.betabuddy.request.RequestsFragment
import kotlin.getValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

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
    private val viewModel: FindFriendsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val et = view.findViewById<EditText>(R.id.etFilterLocation)
        val rv = view.findViewById<RecyclerView>(R.id.rvResults)
        rv.layoutManager = LinearLayoutManager(requireContext())

        val adapter = SimpleResultsAdapter(
            onViewInfo = { pos ->
                // get the selected hit (email + User)
                val hit = viewModel.hits.value?.getOrNull(pos) ?: return@SimpleResultsAdapter
                parentFragmentManager.commit {
                    replace(
                        R.id.fragment_container_view,
                        ProfileFragment().apply {
                            arguments = Bundle().apply {
                                // pass the doc id (email) to your profile screen
                                putString("email", hit.email)
                            }
                        }
                    )
                    addToBackStack(null)
                }
            },
            onSendRequest = { pos ->
                val hit = viewModel.hits.value?.getOrNull(pos) ?: return@SimpleResultsAdapter
                // send to recipient's email (doc id)
                viewModel.sendRequest(hit.email)
                // the repo removes this row from the list on success
            }
        )
        rv.adapter = adapter

        // Observe pretty strings to render the rows
        viewModel.resultRows.observe(viewLifecycleOwner) { rows ->
            adapter.submit(rows)
        }

        // Search button -> triggers Firestore query
        view.findViewById<Button>(R.id.btnSearch).setOnClickListener {
            viewModel.search(et.text?.toString())
        }

        // Pending Requests -> just navigate (listener runs inside RequestsFragment)
        view.findViewById<Button>(R.id.btnPending).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, RequestsFragment())
                addToBackStack(null)
            }
        }

        // Friends list
        view.findViewById<Button>(R.id.btnViewFriends).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, FriendsFragment())
                addToBackStack(null)
            }
        }

        // Initial load: show everyone
        if (savedInstanceState == null) viewModel.search(null)
    }
}

/** Adapter that returns the clicked row position */
private class SimpleResultsAdapter(
    val onViewInfo: (Int) -> Unit,
    val onSendRequest: (Int) -> Unit
) : RecyclerView.Adapter<TextRowVH>() {

    private val data = mutableListOf<String>()

    fun submit(items: List<String>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): TextRowVH {
        val v = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.row_find_result, parent, false)
        return TextRowVH(v, onViewInfo, onSendRequest)
    }

    override fun onBindViewHolder(holder: TextRowVH, position: Int) =
        holder.bind(data[position], position)

    override fun getItemCount() = data.size
}

private class TextRowVH(
    itemView: android.view.View,
    val onViewInfo: (Int) -> Unit,
    val onSendRequest: (Int) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    fun bind(text: String, pos: Int) {
        itemView.findViewById<android.widget.TextView>(R.id.tvName).text = text
        itemView.findViewById<android.widget.TextView>(R.id.tvLocation).text = ""

        // If you later add a dedicated "View Info" button, wire it here:
        val viewInfoId = itemView.resources.getIdentifier("btnViewInfo", "id", itemView.context.packageName)
        if (viewInfoId != 0) {
            itemView.findViewById<android.widget.Button>(viewInfoId)?.setOnClickListener { onViewInfo(pos) }
        }

        itemView.findViewById<android.widget.Button>(R.id.btnRequest)
            .setOnClickListener { onSendRequest(pos) }
    }
}

