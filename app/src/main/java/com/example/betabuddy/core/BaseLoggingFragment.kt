package com.example.betabuddy.core
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment

open class BaseLoggingFragment(layoutId: Int) : Fragment(layoutId) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Lifecycle", "${this::class.java.simpleName} - onCreate")
    }
    override fun onStart() {
        super.onStart()
        Log.d("Lifecycle", "${this::class.java.simpleName} - onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Lifecycle", "${this::class.java.simpleName} - onResume")
    }
    override fun onPause() {
        super.onPause()
        Log.d("Lifecycle", "${this::class.java.simpleName} - onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Lifecycle", "${this::class.java.simpleName} - onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Lifecycle", "${this::class.java.simpleName} - onDestroy")
    }
}
