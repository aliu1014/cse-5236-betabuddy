package com.example.betabuddy.profile

import android.app.AlertDialog
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.viewModels
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import java.io.IOException
import java.util.Locale

/**
 * Displays and edits the full climbing profile for the current user.
 * Location comes from:
 *  - Text box (etLocation), and/or
 *  - MapLocationFragment (lat/lng + label)
 */
class ProfileFragment : BaseLoggingFragment(R.layout.fragment_profile) {

    private val viewModel: ProfileViewModel by viewModels()

    // Coordinates picked on the map (or geocoded from text)
    private var pickedLat: Double? = null
    private var pickedLng: Double? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI refs
        val etUsername      = view.findViewById<EditText>(R.id.etUsername)
        val etName          = view.findViewById<EditText>(R.id.etName)
        val spGender        = view.findViewById<Spinner>(R.id.spGender)
        val etAge           = view.findViewById<EditText>(R.id.etAge)
        val etHeightFeet    = view.findViewById<EditText>(R.id.etHeightFeet)
        val etHeightInches  = view.findViewById<EditText>(R.id.etHeightInches)
        val etWeight        = view.findViewById<EditText>(R.id.etWeight)
        val spGradeTopRope  = view.findViewById<Spinner>(R.id.spGradeTopRope)
        val spGradeLead     = view.findViewById<Spinner>(R.id.spGradeLead)
        val spGradeBoulder  = view.findViewById<Spinner>(R.id.spGradeBoulder)
        val cbHasGear       = view.findViewById<CheckBox>(R.id.cbHasGear)
        val cbTopRopeCert   = view.findViewById<CheckBox>(R.id.cbTopRopeCert)
        val cbLeadCert      = view.findViewById<CheckBox>(R.id.cbLeadCert)
        val etLocation      = view.findViewById<EditText>(R.id.etLocation)
        val btnPickOnMap    = view.findViewById<Button>(R.id.btnPickOnMap)
        val btnSave         = view.findViewById<Button>(R.id.btnSaveProfile)
        val btnDelete       = view.findViewById<Button>(R.id.btnDeleteProfile)

        // Spinner helper
        fun setupSpinner(spinner: Spinner, arrayRes: Int) {
            val adapter = object : ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                resources.getStringArray(arrayRes)
            ) {
                override fun isEnabled(position: Int): Boolean {
                    // Disable the first item ("Select ...")
                    return position != 0
                }

                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    val v = super.getDropDownView(position, convertView, parent)
                    val tv = v as TextView
                    tv.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                    return v
                }
            }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.setSelection(0)
        }

        setupSpinner(spGender,       R.array.gender_options)
        setupSpinner(spGradeTopRope, R.array.toprope_grades)
        setupSpinner(spGradeLead,    R.array.lead_grades)
        setupSpinner(spGradeBoulder, R.array.boulder_grades)

        // Listen for map selection results
        parentFragmentManager.setFragmentResultListener(
            "pick_location",
            viewLifecycleOwner
        ) { _, bundle ->
            pickedLat = bundle.getDouble("lat")
            pickedLng = bundle.getDouble("lng")
            val label = bundle.getString("label") ?: ""
            etLocation.setText(label)
        }

        // Open map picker
        btnPickOnMap.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container_view,
                    MapLocationFragment()
                )
                .addToBackStack(null)
                .commit()
        }

        // Pre-fill from Firestore
        viewModel.user.observe(viewLifecycleOwner) { u ->
            if (u != null) {
                etUsername.setText(u.username)
                etName.setText(u.name)
                etAge.setText(if (u.age != 0) u.age.toString() else "")
                etWeight.setText(if (u.weight != 0) u.weight.toString() else "")

                etHeightFeet.setText(if (u.feet > 0) u.feet.toString() else "")
                etHeightInches.setText(if (u.inches > 0) u.inches.toString() else "")
                etLocation.setText(u.location)

                // Restore saved coords for this user
                pickedLat = u.latitude
                pickedLng = u.longitude

                (spGender.adapter as ArrayAdapter<String>).getPosition(u.gender).let {
                    if (it >= 0) spGender.setSelection(it)
                }
                (spGradeTopRope.adapter as ArrayAdapter<String>).getPosition(u.gradeTopRope).let {
                    if (it >= 0) spGradeTopRope.setSelection(it)
                }
                (spGradeBoulder.adapter as ArrayAdapter<String>).getPosition(u.gradeBoulder).let {
                    if (it >= 0) spGradeBoulder.setSelection(it)
                }
                (spGradeLead.adapter as ArrayAdapter<String>).getPosition(u.gradeLead).let {
                    if (it >= 0) spGradeLead.setSelection(it)
                }

                cbHasGear.isChecked     = u.hasGear
                cbTopRopeCert.isChecked = u.hasTopRopeCert
                cbLeadCert.isChecked    = u.hasLeadCert
            }
        }

// SAVE PROFILE
        btnSave.setOnClickListener {
            val username = etUsername.text.toString().trim()
            if (username.isEmpty()) {
                Toast.makeText(requireContext(), "Username is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val feet        = etHeightFeet.text.toString().toIntOrNull() ?: 0
            val inches      = etHeightInches.text.toString().toIntOrNull() ?: 0
            val locationTxt = etLocation.text.toString().trim()

            // Start with any coordinates we might already have (for example, from the map)
            var lat = pickedLat
            var lng = pickedLng

            // Always try to geocode the current text if it is not empty.
            // This ensures that changing "Columbus, OH" to "Miami, FL" updates the coordinates.
            if (locationTxt.isNotEmpty()) {
                try {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val results = geocoder.getFromLocationName(locationTxt, 1)
                    if (!results.isNullOrEmpty()) {
                        lat = results[0].latitude
                        lng = results[0].longitude
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        requireContext(),
                        "Could not look up that location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // Remember the last resolved coordinates in the fragment
            pickedLat = lat
            pickedLng = lng

            val user = User(
                username      = username,
                name          = etName.text.toString().trim(),
                gender        = spGender.selectedItem.toString(),
                age           = etAge.text.toString().toIntOrNull() ?: 0,
                feet          = feet,
                inches        = inches,
                weight        = etWeight.text.toString().toIntOrNull() ?: 0,
                gradeTopRope  = spGradeTopRope.selectedItem.toString(),
                gradeBoulder  = spGradeBoulder.selectedItem.toString(),
                gradeLead     = spGradeLead.selectedItem.toString(),
                hasGear       = cbHasGear.isChecked,
                hasTopRopeCert= cbTopRopeCert.isChecked,
                hasLeadCert   = cbLeadCert.isChecked,
                location      = locationTxt,
                latitude      = lat,
                longitude     = lng
            )

            // First: ensure username not taken
            viewModel.usernameExists(username) { exists ->
                if (exists) {
                    Toast.makeText(
                        requireContext(),
                        "Username already taken!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    viewModel.saveUser(user) { ok ->
                        Toast.makeText(
                            requireContext(),
                            if (ok) "Profile saved!" else "Save failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (ok) viewModel.loadUser()
                    }
                }
            }
        }

        // DELETE PROFILE
        btnDelete.setOnClickListener {
            val username = etUsername.text.toString().trim()

            if (username.isEmpty()) {
                Toast.makeText(requireContext(), "No username to delete.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.deleteUser(username) { ok ->
                        if (ok) {
                            Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack(
                                null,
                                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
                            )
                            parentFragmentManager.beginTransaction()
                                .replace(
                                    R.id.fragment_container_view,
                                    com.example.betabuddy.login.LoginFragment()
                                )
                                .commit()
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete profile", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Initial load
        viewModel.loadUser()
    }

    companion object {
        fun forUser(username: String) = ProfileFragment().apply {
            arguments = Bundle().apply {
                putString("username", username)
            }
        }
    }
}