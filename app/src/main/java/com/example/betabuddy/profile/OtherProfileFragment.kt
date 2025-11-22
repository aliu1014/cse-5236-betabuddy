package com.example.betabuddy.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import com.example.betabuddy.friends.FindFriendsFragment

class OtherProfileFragment : BaseLoggingFragment(R.layout.fragment_other_profile) {

    private val repo = ProfileRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = arguments?.getString(ARG_EMAIL)
        if (email.isNullOrBlank()) {
            Toast.makeText(requireContext(), "No user specified", Toast.LENGTH_SHORT).show()
            return
        }

        // Grab the views
        val tvName     = view.findViewById<TextView>(R.id.tvName)
        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val tvAge      = view.findViewById<TextView>(R.id.tvAge)
        val tvGender   = view.findViewById<TextView>(R.id.tvGender)
        val tvHeight   = view.findViewById<TextView>(R.id.tvHeight)
        val tvWeight   = view.findViewById<TextView>(R.id.tvWeight)
        val tvLocation = view.findViewById<TextView>(R.id.tvLocation)
        val tvTopRope  = view.findViewById<TextView>(R.id.tvTopRope)
        val tvBoulder  = view.findViewById<TextView>(R.id.tvBoulder)
        val tvLead     = view.findViewById<TextView>(R.id.tvLead)
        val tvGear     = view.findViewById<TextView>(R.id.tvGear)
        val tvTopCert  = view.findViewById<TextView>(R.id.tvTopCert)
        val tvLeadCert = view.findViewById<TextView>(R.id.tvLeadCert)

        // Set up the Back button to navigate back to the FindFriendsFragment
        view.findViewById<Button>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, FindFriendsFragment())
                .addToBackStack(null)
                .commit()
        }

        repo.getUserByEmail(email) { u ->
            if (u == null) {
                Toast.makeText(requireContext(), "Could not load profile", Toast.LENGTH_SHORT).show()
                return@getUserByEmail
            }

            tvName.text     = u.name.ifBlank { "Unknown name" }
            tvUsername.text = u.username
            tvAge.text      = if (u.age > 0) "${u.age}" else "—"
            tvGender.text   = u.gender.ifBlank { "—" }
            tvHeight.text   =
                if (u.feet > 0 || u.inches > 0) "${u.feet}' ${u.inches}\"" else "—"
            tvWeight.text   = if (u.weight > 0) "${u.weight} lbs" else "—"
            tvLocation.text = u.location.ifBlank { "Anywhere" }
            tvTopRope.text  = u.gradeTopRope.ifBlank { "—" }
            tvBoulder.text  = u.gradeBoulder.ifBlank { "—" }
            tvLead.text     = u.gradeLead.ifBlank { "—" }
            tvGear.text     = if (u.hasGear) "Has climbing gear" else "No gear listed"
            tvTopCert.text  = if (u.hasTopRopeCert) "Top rope certified" else "No top rope cert"
            tvLeadCert.text = if (u.hasLeadCert) "Lead certified" else "No lead cert"
        }
    }

    companion object {
        private const val ARG_EMAIL = "email"

        fun forEmail(email: String) = OtherProfileFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_EMAIL, email)
            }
        }
    }
}
