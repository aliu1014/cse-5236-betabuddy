package com.example.betabuddy.home

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.commit
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import com.example.betabuddy.friends.FindFriendsFragment
import com.example.betabuddy.friendlist.FriendsFragment
import com.example.betabuddy.login.LoginFragment
import com.example.betabuddy.profile.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : BaseLoggingFragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        // --- Firestore smoke test with anonymous auth ---
//        val auth = FirebaseAuth.getInstance()
//        val db = Firebase.firestore
//
//        fun doSmokeWrite() {package com.example.betabuddy.home
//
//import android.os.Bundle
//import android.view.View
//import android.widget.Button
//import android.widget.Toast
//import androidx.fragment.app.commit
//import com.example.betabuddy.R
//import com.example.betabuddy.core.BaseLoggingFragment
//import com.example.betabuddy.friends.FindFriendsFragment
//import com.example.betabuddy.friendlist.FriendsFragment
//import com.example.betabuddy.login.LoginFragment
//import com.example.betabuddy.profile.ProfileFragment
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
//
//class HomeFragment : BaseLoggingFragment(R.layout.fragment_home) {
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
////        // --- Firestore smoke test with anonymous auth ---
////        val auth = FirebaseAuth.getInstance()
////        val db = Firebase.firestore
////
////        fun doSmokeWrite() {
////            val testData = hashMapOf(
////                "hello" to "world",
////                "timestamp" to System.currentTimeMillis()
////            )
////            db.collection("debug").document("smokeTest")
////                .set(testData)
////                .addOnSuccessListener {
////                    Toast.makeText(requireContext(), "Firestore write successful!", Toast.LENGTH_SHORT).show()
////                }
////                .addOnFailureListener { e ->
////                    Toast.makeText(requireContext(), "Firestore error: ${e.message}", Toast.LENGTH_LONG).show()
////                }
////        }
////
////        if (auth.currentUser == null) {
////            Toast.makeText(requireContext(), "Please log in to continue.", Toast.LENGTH_SHORT).show()
////            parentFragmentManager.commit {
////                replace(R.id.fragment_container_view, LoginFragment())
////                addToBackStack(null)
////            }
////        } else {
////            doSmokeWrite()
////        }
////        // --- end smoke test ---
//
//        // Navigation buttons (unchanged)
//        view.findViewById<Button>(R.id.btnFindFriends).setOnClickListener {
//            parentFragmentManager.commit {
//                replace(R.id.fragment_container_view, FindFriendsFragment())
//                addToBackStack(null)
//            }
//        }
//        view.findViewById<Button>(R.id.btnViewProfile).setOnClickListener {
//            parentFragmentManager.commit {
//                replace(R.id.fragment_container_view, ProfileFragment())
//                addToBackStack(null)
//            }
//        }
//        view.findViewById<Button>(R.id.btnViewFriends).setOnClickListener {
//            parentFragmentManager.commit {
//                replace(R.id.fragment_container_view, FriendsFragment())
//                addToBackStack(null)
//            }
//        }
//
//        // LOGOUT BUTTON
//        val logoutBtn = view.findViewById<Button>(R.id.btnLogout)
//        logoutBtn.setOnClickListener {
//            // 1. Sign out from Firebase
//            FirebaseAuth.getInstance().signOut()
//
//            // 2. Clear the back stack so back cannot return to Home
//            parentFragmentManager.popBackStack(
//                null,
//                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
//            )
//
//            // 3. Go to LoginFragment without adding it to back stack
//            parentFragmentManager.commit {
//                replace(R.id.fragment_container_view, LoginFragment())
//            }
//
//            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
//        }
//    }
//}
//            val testData = hashMapOf(
//                "hello" to "world",
//                "timestamp" to System.currentTimeMillis()
//            )
//            db.collection("debug").document("smokeTest")
//                .set(testData)
//                .addOnSuccessListener {
//                    Toast.makeText(requireContext(), "Firestore write successful!", Toast.LENGTH_SHORT).show()
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(requireContext(), "Firestore error: ${e.message}", Toast.LENGTH_LONG).show()
//                }
//        }
//
//        if (auth.currentUser == null) {
//            Toast.makeText(requireContext(), "Please log in to continue.", Toast.LENGTH_SHORT).show()
//            parentFragmentManager.commit {
//                replace(R.id.fragment_container_view, LoginFragment())
//                addToBackStack(null)
//            }
//        } else {
//            doSmokeWrite()
//        }
//        // --- end smoke test ---

        // Navigation buttons (unchanged)
//        view.findViewById<Button>(R.id.btnFindFriends).setOnClickListener {
//            parentFragmentManager.commit {
//                replace(R.id.fragment_container_view, FindFriendsFragment())
//                addToBackStack(null)
//            }
//        }
        view.findViewById<View>(R.id.btnFindFriends).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, FindFriendsFragment())
                addToBackStack(null)
            }
        }

        view.findViewById<View>(R.id.btnViewProfile).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, ProfileFragment())
                addToBackStack(null)
            }
        }
        view.findViewById<View>(R.id.btnViewFriends).setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, FriendsFragment())
                addToBackStack(null)
            }
        }

        // LOGOUT BUTTON
        val logoutBtn = view.findViewById<View>(R.id.btnLogout)
        logoutBtn.setOnClickListener {
            // 1. Sign out from Firebase
            FirebaseAuth.getInstance().signOut()

            // 2. Clear the back stack so back cannot return to Home
            parentFragmentManager.popBackStack(
                null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
            )

            // 3. Go to LoginFragment without adding it to back stack
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, LoginFragment())
            }

            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
        }
    }
}