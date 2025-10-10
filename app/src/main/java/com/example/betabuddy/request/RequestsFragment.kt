package com.example.betabuddy.requests

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import android.widget.Button
import com.example.betabuddy.find.FindFriendsFragment


/**
 * Requests Fragment
 * -------------
 * This fragment handles the pending friend requests with the mock data
 * The UI Contains:
 * -A row for each user that sent a request
 * -A button for accepting, a button for declining the request
 */
class RequestsFragment : BaseLoggingFragment(R.layout.fragment_requests) {

    //Sets up the RecyclerView with mock request data and configures the back button behavior
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Initialize RecyclerView and assign a layout manager for vertical scrolling
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rvRequests)
        rv.layoutManager = LinearLayoutManager(requireContext())

        rv.adapter = RequestsAdapter(listOf("Alex", "Priya")) // mock
        // Set up the Back button to navigate back to the FindFriendsFragment
        view.findViewById<Button>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, FindFriendsFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}


//Adapter for displaying the list of friend requests in the RecyclerView
private class RequestsAdapter(private val items: List<String>) :
    RecyclerView.Adapter<RequestVH>() {
    override fun onCreateViewHolder(p: android.view.ViewGroup, v: Int): RequestVH {
        val v = android.view.LayoutInflater.from(p.context)
            .inflate(R.layout.row_request, p, false)
        return RequestVH(v)
    }
    //Binds data, a requester's name to the ViewHolder for the given position
    override fun onBindViewHolder(h: RequestVH, i: Int) = h.bind(items[i])
    override fun getItemCount() = items.size
}

 //ViewHolder class for representing a single friend request row
private class RequestVH(item: android.view.View) : RecyclerView.ViewHolder(item) {
    //Binds the requester's name to the text view in the row
    fun bind(name: String) {
        itemView.findViewById<android.widget.TextView>(R.id.tvRequester).text = name
        // Buttons are present; no real logic yet
    }
}
