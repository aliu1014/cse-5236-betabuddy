package com.example.betabuddy.requests

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import android.widget.Button
import com.example.betabuddy.find.FindFriendsFragment

class RequestsFragment : BaseLoggingFragment(R.layout.fragment_requests) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rvRequests)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = RequestsAdapter(listOf("Alex", "Priya")) // mock

        // Back button
        view.findViewById<Button>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, FindFriendsFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}

private class RequestsAdapter(private val items: List<String>) :
    RecyclerView.Adapter<RequestVH>() {
    override fun onCreateViewHolder(p: android.view.ViewGroup, v: Int): RequestVH {
        val v = android.view.LayoutInflater.from(p.context)
            .inflate(R.layout.row_request, p, false)
        return RequestVH(v)
    }
    override fun onBindViewHolder(h: RequestVH, i: Int) = h.bind(items[i])
    override fun getItemCount() = items.size
}
private class RequestVH(item: android.view.View) : RecyclerView.ViewHolder(item) {
    fun bind(name: String) {
        itemView.findViewById<android.widget.TextView>(R.id.tvRequester).text = name
        // Buttons are present; no real logic yet
    }
}
