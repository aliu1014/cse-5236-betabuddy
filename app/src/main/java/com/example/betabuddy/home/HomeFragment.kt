package com.example.betabuddy.home

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.commit
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import com.example.betabuddy.find.FindFriendsFragment
import com.example.betabuddy.friendlist.FriendsFragment
import com.example.betabuddy.login.LoginFragment
import com.example.betabuddy.profile.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : BaseLoggingFragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Firestore smoke test with anonymous auth ---
        val auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        fun doSmokeWrite() {
            val testData = hashMapOf(
                "hello" to "world",
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("debug").document("smokeTest")
                .set(testData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Firestore write successful!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Firestore error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        if (auth.currentUser == null) {
            Toast.makeText(requireContext(), "Please log in to continue.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, LoginFragment())
                addToBackStack(null)
            }
        } else {
            doSmokeWrite()
        }
        // --- end smoke test ---

        // Navigation buttons (unchanged)
        view.findViewById<Button>(R.id.btnFindFriends).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, FindFriendsFragment())
                addToBackStack(null)
            }
        }
        view.findViewById<Button>(R.id.btnViewProfile).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, ProfileFragment())
                addToBackStack(null)
            }
        }
        view.findViewById<Button>(R.id.btnViewFriends).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, FriendsFragment())
                addToBackStack(null)
            }
        }
    }
}





//package com.example.betabuddy.home
//
//import android.os.Bundle
//import android.view.View
//import android.widget.Button
//import androidx.fragment.app.commit
//import com.example.betabuddy.R
//import com.example.betabuddy.core.BaseLoggingFragment
//import com.example.betabuddy.find.FindFriendsFragment
//import com.example.betabuddy.friendlist.FriendsFragment
//import com.example.betabuddy.profile.ProfileFragment
//
//import android.widget.Toast
//import com.google.firebase.ktx.Firebase
//import com.google.firebase.firestore.ktx.firestore
//
//
///**
// * Home Fragment
// * -------------
// * This fragment provides a space where the user is able to navigate to the different features such as:
// * Find Friends, View Profile, View Friend List
// * The UI Contains:
// * -A button navigating to Find Friends Page
// * -A button navigating to View Profile Page
// * -A button navigating to View Friends Page
// */
//
//class HomeFragment : BaseLoggingFragment(R.layout.fragment_home) {
//
//    //Sets up the button click listeners for navigating to different sections of the app.
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        //Navigates to Find Friends page, where they can search for and send requests
//        view.findViewById<Button>(R.id.btnFindFriends).setOnClickListener {
//            parentFragmentManager.commit {
//                replace(R.id.fragment_container_view, FindFriendsFragment())
//                addToBackStack(null)
//            }
//        }
//        //Navigates to Profile page, where they can view and edit profile details
//        view.findViewById<Button>(R.id.btnViewProfile).setOnClickListener {
//            parentFragmentManager.commit {
//                replace(R.id.fragment_container_view, ProfileFragment())
//                addToBackStack(null)
//            }
//        }
//        //Navigates to View Friends page, where they can view their current friend list
//        view.findViewById<Button>(R.id.btnViewFriends).setOnClickListener {
//            parentFragmentManager.commit {
//                replace(R.id.fragment_container_view, FriendsFragment())
//                addToBackStack(null)
//            }
//        }
//    }
//}
