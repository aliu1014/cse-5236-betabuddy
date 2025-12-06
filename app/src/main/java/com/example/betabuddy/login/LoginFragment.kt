package com.example.betabuddy.login

import com.example.betabuddy.R
import com.example.betabuddy.core.BaseLoggingFragment
import com.example.betabuddy.home.HomeFragment
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import android.widget.ImageButton

/**
 * Login Fragment
 * --------------
 * This fragment provides the login screen, where the user will log in or sign up
 * The UI Contains:
 * -A text box for email
 * -A text box for password
 * -A button to log in
 * -A button to sign up
 */
class LoginFragment : BaseLoggingFragment(R.layout.fragment_login){
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Connect UI elements from xml
        val usernameInput = view.findViewById<EditText>(R.id.etEmail)
        val passwordInput = view.findViewById<EditText>(R.id.etPassword)
        val loginButton = view.findViewById<Button>(R.id.btnLogin)
        val signupButton = view.findViewById<TextView>(R.id.btnSignup)
        val tvLoginError  = view.findViewById<TextView>(R.id.tvLoginError)

        // Handle pressing Enter (Done) on keyboard to log in properly
        passwordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val email = usernameInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()

                // Validate input before attempting sign-in
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
                    return@setOnEditorActionListener true
                }

                // Use the same sign-in logic (no navigation yet)
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //Navigate to home screen on successful login
                            Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container_view, HomeFragment())
                                .addToBackStack(null)
                                .commit()
                        } else {
                            val message = task.exception?.message ?: "Login failed"
                            when {
                                message.contains("no user record", ignoreCase = true) ->
                                    Toast.makeText(requireContext(), "No account found. Please sign up.", Toast.LENGTH_LONG).show()

                                message.contains("password", ignoreCase = true) ->
                                    Toast.makeText(requireContext(), "Incorrect password. Try again.", Toast.LENGTH_LONG).show()

                                else ->
                                    Toast.makeText(requireContext(), "Incorrect email or password.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                true // consume event
            } else {
                false
            }
        }

        // Handle Login button press
        loginButton.setOnClickListener {
            val email = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                // Hide error if inputs are now valid
                tvLoginError.visibility = View.GONE
            }
            //Attempt to sign in with Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //Navigate to home screen upon success
                        Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container_view, HomeFragment())
                            .addToBackStack(null)
                            .commit()
                    } else {
                        //Handle sign in errors
                        val message = task.exception?.message ?: "Login failed"
                        when {
                            message.contains("no user record", ignoreCase = true) ->
                                Toast.makeText(requireContext(), "No account found. Please sign up.", Toast.LENGTH_LONG).show()

                            message.contains("password", ignoreCase = true) ->
                                Toast.makeText(requireContext(), "Incorrect password. Try again.", Toast.LENGTH_LONG).show()

                            else ->
                                Toast.makeText(requireContext(), "Incorrect email or password.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }

        // Handle Signup
        signupButton.setOnClickListener {
            val email = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Create new account in Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()
                        //Inform user and navigate to home
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container_view, HomeFragment())
                            .addToBackStack(null)
                            .commit()
                    } else {
                        Toast.makeText(requireContext(), "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }


    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, HomeFragment())
                        .addToBackStack(null)
                        .commit()
                } else {
                    val message = task.exception?.message ?: "Login failed"
                    if (message.contains("no user record", ignoreCase = true)) {
                        Toast.makeText(requireContext(), "No account found. Please sign up.", Toast.LENGTH_LONG).show()
                    } else if (message.contains("password", ignoreCase = true)) {
                        Toast.makeText(requireContext(), "Incorrect password. Try again.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Incorrect email or password.", Toast.LENGTH_LONG).show()
                    }
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
