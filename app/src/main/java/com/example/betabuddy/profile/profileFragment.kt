package com.example.betabuddy.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment

class ProfileFragment : BaseLoggingFragment(R.layout.fragment_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val name = view.findViewById<EditText>(R.id.etName)
        val loc = view.findViewById<EditText>(R.id.etLocation)
        val skill = view.findViewById<EditText>(R.id.etSkill)
        view.findViewById<Button>(R.id.btnSaveProfile).setOnClickListener {
            Toast.makeText(requireContext(),
                "Saved: ${name.text} / ${loc.text} / ${skill.text}",
                Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        // for future: ProfileFragment.forUser(uid: String)
        fun forUser(uid: String) = ProfileFragment()
    }
}