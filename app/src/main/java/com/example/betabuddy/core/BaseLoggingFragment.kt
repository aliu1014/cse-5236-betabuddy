package com.example.betabuddy.core
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import com.example.betabuddy.R
import com.example.betabuddy.home.HomeFragment
import android.widget.Button


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
