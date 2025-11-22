package com.example.betabuddy

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.betabuddy.login.LoginFragment
import com.google.android.libraries.places.api.Places



/**
 * MainActivity
 * -------------
 * This is the entry point of the BetaBuddy app.
 * It hosts the fragments that make up the app’s UI and manages their lifecycle.
 *
 * Responsibilities:
 * - Initialize the app layout
 * - Load the initial fragment, the login fragment
 * - Log Android activity lifecycle events for debugging and tracking
 */
class MainActivity : AppCompatActivity() {
    private val tag = "ActivityLifecycle:MainActivity"

    //Sets up the UI layout and loads the LoginFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }
        Log.d(tag, "onCreate()")
        setContentView(R.layout.activity_main)
        //Only adds LoginFragment if there is no svaed instance state
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, LoginFragment())
                .commitNow()
        }
    }

    //Start UI updates or animations
    override fun onStart()  { super.onStart();  Log.d(tag, "onStart()") }
    //Occurs when the activity starts interacting with the user
    override fun onResume() { super.onResume(); Log.d(tag, "onResume()") }
    //Used to pause ongoing tasks or animations
    override fun onPause()  { Log.d(tag, "onPause()");  super.onPause() }
    //Used to release resources that aren't needed while paused
    override fun onStop()   { Log.d(tag, "onStop()");   super.onStop() }
    //Used for final cleanup, closes connections and releases resources
    override fun onDestroy(){ Log.d(tag, "onDestroy()");super.onDestroy() }
}
