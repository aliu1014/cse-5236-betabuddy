package com.example.betabuddy.login

import androidx.fragment.app.Fragment
import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult


class LoginFragment : BaseLoggingFragment(R.layout.fragment_login){
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Connect UI elements
        val usernameInput = view.findViewById<EditText>(R.id.usernameInput)
        val passwordInput = view.findViewById<EditText>(R.id.passwordInput)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val signupButton = view.findViewById<Button>(R.id.btnSignup)

        // Handle Login
        loginButton.setOnClickListener {
            val email = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signInUser(email, password)
            } else {
                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Signup
        signupButton.setOnClickListener {
            val email = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                createUser(email, password)
            } else {
                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> ->
            if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    // TODO: navigate to home screen here
                } else {
                    Toast.makeText(requireContext(), "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> ->
            if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()
                    // TODO: navigate to home screen here
                } else {
                    Toast.makeText(requireContext(), "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
