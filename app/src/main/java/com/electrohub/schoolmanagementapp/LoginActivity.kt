package com.electrohub.schoolmanagementapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Setup edge-to-edge padding
        findViewById<View>(R.id.main)?.let { mainView ->
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
            applyBounceToAllClickables(mainView)
        }

        // Find views
        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val errorText = findViewById<TextView>(R.id.errorText)
        val registerText = findViewById<TextView>(R.id.registerText)

        // Set login click listener
        loginButton.setOnClickListener {
            val email = emailInput?.text.toString().trim()
            val password = passwordInput?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                errorText.text = "Please enter email and password"
                errorText.visibility = View.VISIBLE
            } else {
                errorText.visibility = View.GONE
                loginButton.text = "Logging in..."
                loginButton.isEnabled = false
                performFirebaseLogin(email, password, errorText, loginButton)
            }
        }

        // Set register click listener
        registerText?.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performFirebaseLogin(email: String, password: String, errorTextView: TextView, loginButton: Button) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        checkFirestoreRegistration(uid, errorTextView, loginButton)
                    }
                } else {
                    loginButton.text = "Log In"
                    loginButton.isEnabled = true
                    errorTextView.text = "Login Failed: ${task.exception?.message}"
                    errorTextView.visibility = View.VISIBLE
                }
            }
    }

    private fun checkFirestoreRegistration(uid: String, errorTextView: TextView, loginButton: Button) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val isRegistered = document.getBoolean("registered") ?: false
                    if (isRegistered) {
                        val fullName = document.getString("fullName") ?: "User"
                        val role = document.getString("role") ?: "Student"
                        
                        Toast.makeText(this, "Welcome back, $fullName", Toast.LENGTH_SHORT).show()
                        
                        // Start the logout monitoring service
                        startService(Intent(this, LogoutService::class.java))
                        
                        val intent = if (role == "Admin") {
                            Intent(this, AdminDashboard::class.java)
                        } else {
                            Intent(this, StudentDashboard::class.java)
                        }

                        intent.putExtra("USER_NAME", fullName)
                        startActivity(intent)
                        finish()
                    } else {
                        loginButton.text = "Log In"
                        loginButton.isEnabled = true
                        errorTextView.text = "Access Denied: Your profile is not fully registered."
                        errorTextView.visibility = View.VISIBLE
                        auth.signOut()
                    }
                } else {
                    loginButton.text = "Log In"
                    loginButton.isEnabled = true
                    errorTextView.text = "Error: User record not found in database."
                    errorTextView.visibility = View.VISIBLE
                    auth.signOut()
                }
            }
            .addOnFailureListener { e ->
                loginButton.text = "Log In"
                loginButton.isEnabled = true
                errorTextView.text = "Database Error: ${e.message}"
                errorTextView.visibility = View.VISIBLE
            }
    }
}
