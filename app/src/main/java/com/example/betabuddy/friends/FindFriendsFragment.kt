package com.example.betabuddy.friends

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.EditText
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import com.example.betabuddy.friendlist.FriendsFragment
import com.example.betabuddy.profile.ProfileFragment
import com.example.betabuddy.profile.OtherProfileFragment
import com.example.betabuddy.request.RequestsFragment
import kotlin.getValue
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient      // NEW
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import android.annotation.SuppressLint
import android.widget.Spinner


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
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastRadiusMiles: Double = 20.0
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            getCurrentLocationAndSearchNearby(lastRadiusMiles)
        } else {
            // Permission denied -> show everybody
            viewModel.searchByCity(null)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val et = view.findViewById<EditText>(R.id.etFilterLocation)
        val rv = view.findViewById<RecyclerView>(R.id.rvResults)
        rv.layoutManager = LinearLayoutManager(requireContext())
        val spRadius = view.findViewById<Spinner>(R.id.spRadiusMiles)
        val btnNearby = view.findViewById<Button>(R.id.btnNearby)

        btnNearby.setOnClickListener {
            val radiusText = spRadius.selectedItem?.toString() ?: "20"
            val radiusMiles = radiusText.toDoubleOrNull() ?: 20.0
            lastRadiusMiles = radiusMiles
            // Use logged-in user's profile location as center
            viewModel.searchNearbyFromProfile(radiusMiles)
        }

        val adapter = SimpleResultsAdapter(
            onViewInfo = { pos ->
                val hit = viewModel.hits.value?.getOrNull(pos) ?: return@SimpleResultsAdapter
                parentFragmentManager.commit {
                    replace(
                        R.id.fragment_container_view,
                        OtherProfileFragment().apply {
                            arguments = Bundle().apply {
                                putString("email", hit.email)
                            }
                        }
                    )
                    addToBackStack(null)
                }
            },
            onSendRequest = { pos ->
                val hit = viewModel.hits.value?.getOrNull(pos) ?: return@SimpleResultsAdapter
                viewModel.sendRequest(hit.email)
            }
        )
        rv.adapter = adapter

        viewModel.resultRows.observe(viewLifecycleOwner) { rows ->
            adapter.submit(rows)
        }

        // Search button:
        // - text entered -> search by city
        // - empty        -> nearby based on my profile location
        view.findViewById<Button>(R.id.btnSearch).setOnClickListener {
            val text = et.text?.toString()?.trim()
            if (!text.isNullOrEmpty()) {
                viewModel.searchByCity(text)
            } else {
                viewModel.searchNearbyFromProfile(lastRadiusMiles)
            }
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

        if (savedInstanceState == null) viewModel.searchByCity(null)
    }

    private fun requestLocationAndSearchNearby(radiusMiles: Double) {
        val ctx = requireContext()
        val fineGranted = ActivityCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ActivityCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            getCurrentLocationAndSearchNearby(radiusMiles)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            // when permissions are granted, call getCurrentLocationAndSearchNearby()
            // from your ActivityResult callback with a default radius if you want
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationAndSearchNearby(radiusMiles: Double) {
        val ctx = requireContext()
        val fineGranted = ActivityCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ActivityCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) return

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.searchNearby(
                        location.latitude,
                        location.longitude,
                        radiusMiles = radiusMiles
                    )
                } else {
                    // fallback – maybe show everyone or same city
                    viewModel.searchByCity(null)
                }
            }
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
        val tvName = itemView.findViewById<android.widget.TextView>(R.id.tvName)
        val tvLocation = itemView.findViewById<android.widget.TextView>(R.id.tvLocation)
        val btnRequest = itemView.findViewById<android.widget.Button>(R.id.btnRequest)

        tvName.text = text
        tvLocation.text = ""   // or keep whatever you were doing here

        // Clicking the **row** opens the profile
        itemView.setOnClickListener { onViewInfo(pos) }

        // (Optional) clicking just the name also opens the profile
        tvName.setOnClickListener { onViewInfo(pos) }

        // Send request button
        btnRequest.setOnClickListener { onSendRequest(pos) }
        val viewInfoId = itemView.resources.getIdentifier("btnViewInfo", "id", itemView.context.packageName)
        if (viewInfoId != 0) {
            itemView.findViewById<android.widget.Button>(viewInfoId)
                ?.setOnClickListener { onViewInfo(pos) }
        }

        itemView.findViewById<android.widget.Button>(R.id.btnRequest)
            .setOnClickListener { onSendRequest(pos) }

    }
}