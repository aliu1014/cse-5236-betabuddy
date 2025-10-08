package com.example.betabuddy.home

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.commit
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import com.example.betabuddy.find.FindFriendsFragment
import com.example.betabuddy.friendlist.FriendsFragment
import com.example.betabuddy.profile.ProfileFragment

class HomeFragment : BaseLoggingFragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
