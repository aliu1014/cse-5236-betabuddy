package com.example.betabuddy.profile

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class MapLocationFragment :
    BaseLoggingFragment(R.layout.fragment_map_location),
    OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var selectedLatLng: LatLng? = null
    private var selectedLabel: String = ""

    private val geocoder by lazy {
        Geocoder(requireContext(), Locale.getDefault())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the embedded map
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val etSearch = view.findViewById<EditText>(R.id.etSearchLocation)
        val btnSearch = view.findViewById<Button>(R.id.btnSearch)
        val btnUse = view.findViewById<Button>(R.id.btnUseThisLocation)

        // 🔍 GO button
        btnSearch.setOnClickListener {
            Toast.makeText(requireContext(), "Searching…", Toast.LENGTH_SHORT).show()

            val query = etSearch.text.toString().trim()
            if (query.isEmpty()) return@setOnClickListener

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val results = withContext(Dispatchers.IO) {
                        geocoder.getFromLocationName(query, 1)
                    }

                    if (results.isNullOrEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "No results for \"$query\"",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val addr = results[0]
                        val latLng = LatLng(addr.latitude, addr.longitude)
                        val label = buildCityState(addr).ifBlank { query }

                        selectedLatLng = latLng
                        selectedLabel = label

                        map.clear()
                        map.addMarker(MarkerOptions().position(latLng).title(label))
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(latLng, 11f)
                        )
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        requireContext(),
                        "Could not look up that place",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // ✅ USE THIS LOCATION
        btnUse.setOnClickListener {
            val latLng = selectedLatLng
            if (latLng == null) {
                Toast.makeText(
                    requireContext(),
                    "Tap on the map or search first",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val labelToSend = selectedLabel.ifBlank {
                    "${latLng.latitude}, ${latLng.longitude}"
                }

                Toast.makeText(
                    requireContext(),
                    "Picked: $labelToSend",
                    Toast.LENGTH_SHORT
                ).show()

                val result = Bundle().apply {
                    putDouble("lat", latLng.latitude)
                    putDouble("lng", latLng.longitude)
                    putString("label", labelToSend)
                }

                // Use same FragmentManager as ProfileFragment
                parentFragmentManager.setFragmentResult("pick_location", result)
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        val usa = LatLng(39.8283, -98.5795)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(usa, 3.5f))

        // Tap on map to move marker
        map.setOnMapClickListener { latLng ->
            selectedLatLng = latLng
            map.clear()
            map.addMarker(MarkerOptions().position(latLng))

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val results = withContext(Dispatchers.IO) {
                        geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    }
                    selectedLabel = if (!results.isNullOrEmpty()) {
                        buildCityState(results[0])
                    } else ""
                } catch (e: IOException) {
                    e.printStackTrace()
                    selectedLabel = ""
                }
            }

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11f))
        }
    }

    private fun buildCityState(addr: Address): String {
        val city = addr.locality ?: addr.subAdminArea ?: ""
        val state = addr.adminArea ?: ""
        return listOf(city, state)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}
