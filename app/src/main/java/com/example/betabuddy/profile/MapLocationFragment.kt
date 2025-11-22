package com.example.betabuddy.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapLocationFragment : BaseLoggingFragment(R.layout.fragment_map_location), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var selectedLatLng: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_map_location, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the embedded SupportMapFragment
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        view.findViewById<Button>(R.id.btnUseThisLocation).setOnClickListener {
            val latLng = selectedLatLng
            if (latLng == null) {
                Toast.makeText(
                    requireContext(),
                    "Tap on the map first",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val result = Bundle().apply {
                    putDouble("lat", latLng.latitude)
                    putDouble("lng", latLng.longitude)
                    // later you can replace this with a reverse-geocoded string
                    putString("label", "Some Label")
                }

                parentFragmentManager.setFragmentResult("pick_location", result)
                parentFragmentManager.popBackStack()

            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        // Optional: start camera roughly on US
        val usa = LatLng(39.8283, -98.5795)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(usa, 3.5f))

        map.setOnMapClickListener { latLng ->
            selectedLatLng = latLng
            map.clear()
            map.addMarker(MarkerOptions().position(latLng))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11f))
        }
    }
}
