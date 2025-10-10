package com.example.betabuddy.core
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import com.example.betabuddy.R
import com.example.betabuddy.home.HomeFragment
import android.widget.Button

/**
 * This is an abstract base class that all fragments in the app inherit from.
 * It serves two main purposes:
 *  1. Logs the fragment lifecycle events.
 *  2. Adds a shared Home button handler so all fragments can navigate back to the Home screen.
 * The class takes a layout resource ID (layoutId) that child fragments pass when extending it.
 */
open class BaseLoggingFragment(layoutId: Int) : Fragment(layoutId) {

    // Called when fragment is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Lifecycle", "${this::class.java.simpleName} - onCreate")
    }

    // Called when fragment becomes visible to user
    override fun onStart() {
        super.onStart()
        Log.d("Lifecycle", "${this::class.java.simpleName} - onStart")
    }

    // Called when fragment is visible and user can interact
    override fun onResume() {
        super.onResume()
        Log.d("Lifecycle", "${this::class.java.simpleName} - onResume")
    }

    // Called when fragment is partially obscured such as when another action appears in front
    override fun onPause() {
        super.onPause()
        Log.d("Lifecycle", "${this::class.java.simpleName} - onPause")
    }

    // Called when fragment is not visible anymore
    override fun onStop() {
        super.onStop()
        Log.d("Lifecycle", "${this::class.java.simpleName} - onStop")
    }

    // Called before fragment is destroyed and its resources are freed
    override fun onDestroy() {
        super.onDestroy()
        Log.d("Lifecycle", "${this::class.java.simpleName} - onDestroy")
    }

    // Called after fragment's view has been created. Looks for home button if in the layout and sets up click listener
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeButton = view.findViewById<Button?>(R.id.btnHome)
        homeButton?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, HomeFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
