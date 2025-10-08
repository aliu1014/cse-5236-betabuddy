package com.example.betabuddy

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.betabuddy.login.LoginFragment

class MainActivity : AppCompatActivity() {
    private val tag = "ActivityLifecycle:MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate()")
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, LoginFragment())
                .commitNow()
        }
    }

    override fun onStart()  { super.onStart();  Log.d(tag, "onStart()") }
    override fun onResume() { super.onResume(); Log.d(tag, "onResume()") }
    override fun onPause()  { Log.d(tag, "onPause()");  super.onPause() }
    override fun onStop()   { Log.d(tag, "onStop()");   super.onStop() }
    override fun onDestroy(){ Log.d(tag, "onDestroy()");super.onDestroy() }
}
