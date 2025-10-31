package com.example.betabuddy.request

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import android.widget.Button
import com.example.betabuddy.friends.FindFriendsFragment
import androidx.fragment.app.viewModels
import com.example.betabuddy.model.FriendRequest


/**
 * Requests Fragment
 * -------------
 * This fragment handles the pending friend requests with the mock data
 * The UI Contains:
 * -A row for each user that sent a request
 * -A button for accepting, a button for declining the request
 */
class RequestsFragment : BaseLoggingFragment(R.layout.fragment_requests) {

    private val vm: RequestsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvRequests).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }

        val adapter = RequestsAdapter(
            onAccept = { req -> vm.accept(req) },
            onDecline = { req -> vm.decline(req) }
        )
        rv.adapter = adapter

        // Observe live pending requests
        vm.requests.observe(viewLifecycleOwner) { list ->
            adapter.submit(list)
        }

        // Optional: back button (if you keep it)
        view.findViewById<Button>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onStart() {
        super.onStart()
        vm.start() // begins Firestore snapshot listener
    }

    override fun onStop() {
        super.onStop()
        vm.stop() // removes listener
    }
}

/** Simple adapter for FriendRequest rows. Expects row_request.xml with tvRequester, btnAccept, btnDecline. */
private class RequestsAdapter(
    private val onAccept: (FriendRequest) -> Unit,
    private val onDecline: (FriendRequest) -> Unit
) : RecyclerView.Adapter<RequestVH>() {

    private val data = mutableListOf<FriendRequest>()

    fun submit(items: List<FriendRequest>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p: android.view.ViewGroup, vType: Int): RequestVH {
        val v = android.view.LayoutInflater.from(p.context)
            .inflate(R.layout.row_request, p, false)
        return RequestVH(v, onAccept, onDecline)
    }

    override fun onBindViewHolder(h: RequestVH, i: Int) = h.bind(data[i])
    override fun getItemCount() = data.size
}

private class RequestVH(
    itemView: android.view.View,
    private val onAccept: (FriendRequest) -> Unit,
    private val onDecline: (FriendRequest) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    fun bind(req: FriendRequest) {
        // Show sender's display name if present, else email
        val who = if (req.senderName.isNotBlank()) req.senderName else req.senderEmail
        itemView.findViewById<android.widget.TextView>(R.id.tvRequester).text = who

        itemView.findViewById<android.widget.Button>(R.id.btnAccept)
            .setOnClickListener { onAccept(req) }

        itemView.findViewById<android.widget.Button>(R.id.btnDecline)
            .setOnClickListener { onDecline(req) }
    }
}

