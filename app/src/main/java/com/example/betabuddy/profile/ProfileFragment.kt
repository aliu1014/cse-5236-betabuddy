package com.example.betabuddy.profile

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.viewModels
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import android.graphics.Color
import android.view.ViewGroup
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * Displays and edits the full climbing profile for the current user.
 * Uses Firestore via ViewModel + Repository for persistence.
 */
class ProfileFragment : BaseLoggingFragment(R.layout.fragment_profile) {

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            getLocationAndSaveProfile()
        } else {
            // User denied location -> save without GPS
            saveProfileWithLocation(null, null)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        // Connect UI elements
        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val etName = view.findViewById<EditText>(R.id.etName)
        val spGender = view.findViewById<Spinner>(R.id.spGender)
        val etAge = view.findViewById<EditText>(R.id.etAge)
        val etHeightFeet = view.findViewById<EditText>(R.id.etHeightFeet)
        val etHeightInches = view.findViewById<EditText>(R.id.etHeightInches)
        val etWeight = view.findViewById<EditText>(R.id.etWeight)
        val spGradeTopRope = view.findViewById<Spinner>(R.id.spGradeTopRope)
        val spGradeLead = view.findViewById<Spinner>(R.id.spGradeLead)
        val spGradeBoulder = view.findViewById<Spinner>(R.id.spGradeBoulder)
        val cbHasGear = view.findViewById<CheckBox>(R.id.cbHasGear)
        val cbTopRopeCert = view.findViewById<CheckBox>(R.id.cbTopRopeCert)
        val cbLeadCert = view.findViewById<CheckBox>(R.id.cbLeadCert)
        val btnSave = view.findViewById<Button>(R.id.btnSaveProfile)
        val etLocation = view.findViewById<EditText>(R.id.etLocation)

        // --- Setup dropdown adapters ---
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

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    val textView = view as TextView
                    if (position == 0) {
                        // Gray out the hint item
                        textView.setTextColor(Color.GRAY)
                    } else {
                        textView.setTextColor(Color.BLACK)
                    }
                    return view
                }
            }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.setSelection(0) // Show hint initially
        }

        setupSpinner(spGender, R.array.gender_options)
        setupSpinner(spGradeTopRope, R.array.toprope_grades)
        setupSpinner(spGradeLead, R.array.lead_grades)
        setupSpinner(spGradeBoulder, R.array.boulder_grades)

        // Observe Firestore user data (LiveData<User>)
        viewModel.user.observe(viewLifecycleOwner) { u ->
            if (u != null) {
                etUsername.setText(u.username)
                etName.setText(u.name)
                etAge.setText(if (u.age != 0) u.age.toString() else "")
                etWeight.setText(if (u.weight != 0) u.weight.toString() else "")

                // Height fields
                etHeightFeet.setText(if (u.feet > 0) u.feet.toString() else "")
                etHeightInches.setText(if (u.inches > 0) u.inches.toString() else "")
                etLocation.setText(u.location)

                // Dropdown selections
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

                // Checkboxes
                cbHasGear.isChecked = u.hasGear
                cbTopRopeCert.isChecked = u.hasTopRopeCert
                cbLeadCert.isChecked = u.hasLeadCert
            }
        }

        // Save Button Logic
        btnSave.setOnClickListener {
            val username = etUsername.text.toString().trim()
            if (username.isEmpty()) {
                Toast.makeText(requireContext(), "Username is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            requestLocationThenSave()

            val feet = etHeightFeet.text.toString().toIntOrNull() ?: 0
            val inches = etHeightInches.text.toString().toIntOrNull() ?: 0

            val user = User(
                username = username,
                name = etName.text.toString().trim(),
                gender = spGender.selectedItem.toString(),
                age = etAge.text.toString().toIntOrNull() ?: 0,
                feet = feet,
                inches = inches,
                weight = etWeight.text.toString().toIntOrNull() ?: 0,
                gradeTopRope = spGradeTopRope.selectedItem.toString(),
                gradeBoulder = spGradeBoulder.selectedItem.toString(),
                gradeLead = spGradeLead.selectedItem.toString(),
                hasGear = cbHasGear.isChecked,
                hasTopRopeCert = cbTopRopeCert.isChecked,
                hasLeadCert = cbLeadCert.isChecked
            )

            viewModel.saveUser(user) { ok ->
                Toast.makeText(
                    requireContext(),
                    if (ok) "Profile saved!" else "Save failed.",
                    Toast.LENGTH_SHORT
                ).show()

                if (ok) viewModel.loadUser() // Refresh after save
            }

            // Check uniqueness before saving
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

        // --- Delete Profile Button ---
        val btnDelete = view.findViewById<Button>(R.id.btnDeleteProfile)

        btnDelete.setOnClickListener {
            val username = view.findViewById<EditText>(R.id.etUsername).text.toString().trim()

            if (username.isEmpty()) {
                Toast.makeText(requireContext(), "No username to delete.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ask user to confirm deletion
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                .setPositiveButton("Yes") { _, _ ->
                    // Call ViewModel delete function
                    viewModel.deleteUser(username) { ok ->
                        if (ok) {
                            Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show()

                            // Clear back stack and reload LoginFragment
                            parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
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
        viewModel.loadUser()
    }
    private fun requestLocationThenSave() {
        val ctx = requireContext()
        val fineGranted = ActivityCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ActivityCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            getLocationAndSaveProfile()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    @SuppressLint("MissingPermission")
    private fun getLocationAndSaveProfile() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    saveProfileWithLocation(location.latitude, location.longitude)
                } else {
                    saveProfileWithLocation(null, null)
                }
            }
    }

    fun saveProfileWithLocation(lat: Double?, lng: Double?) {
        val v = view ?: return

        val etUsername = v.findViewById<EditText>(R.id.etUsername)
        val etName = v.findViewById<EditText>(R.id.etName)
        val spGender = v.findViewById<Spinner>(R.id.spGender)
        val etAge = v.findViewById<EditText>(R.id.etAge)
        val etHeightFeet = v.findViewById<EditText>(R.id.etHeightFeet)
        val etHeightInches = v.findViewById<EditText>(R.id.etHeightInches)
        val etWeight = v.findViewById<EditText>(R.id.etWeight)
        val spGradeTopRope = v.findViewById<Spinner>(R.id.spGradeTopRope)
        val spGradeLead = v.findViewById<Spinner>(R.id.spGradeLead)
        val spGradeBoulder = v.findViewById<Spinner>(R.id.spGradeBoulder)
        val cbHasGear = v.findViewById<CheckBox>(R.id.cbHasGear)
        val cbTopRopeCert = v.findViewById<CheckBox>(R.id.cbTopRopeCert)
        val cbLeadCert = v.findViewById<CheckBox>(R.id.cbLeadCert)
        val etLocation = v.findViewById<EditText>(R.id.etLocation)

        val username = etUsername.text.toString().trim()
        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Username is required", Toast.LENGTH_SHORT).show()
            return
        }

        val feet = etHeightFeet.text.toString().toIntOrNull() ?: 0
        val inches = etHeightInches.text.toString().toIntOrNull() ?: 0
        val locationText = etLocation.text.toString().trim()

        val user = User(
            username = username,
            name = etName.text.toString().trim(),
            gender = spGender.selectedItem.toString(),
            age = etAge.text.toString().toIntOrNull() ?: 0,
            feet = feet,
            inches = inches,
            weight = etWeight.text.toString().toIntOrNull() ?: 0,
            gradeTopRope = spGradeTopRope.selectedItem.toString(),
            gradeBoulder = spGradeBoulder.selectedItem.toString(),
            gradeLead = spGradeLead.selectedItem.toString(),
            hasGear = cbHasGear.isChecked,
            hasTopRopeCert = cbTopRopeCert.isChecked,
            hasLeadCert = cbLeadCert.isChecked,
            // NEW: GPS fields
            location = locationText,
            latitude = lat,
            longitude = lng
        )

        // Check username uniqueness, then save
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
    companion object {
        fun forUser(username: String) = ProfileFragment()
    }
}