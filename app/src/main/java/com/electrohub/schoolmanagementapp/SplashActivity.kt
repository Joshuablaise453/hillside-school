package com.electrohub.schoolmanagementapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // Start the logout monitoring service
                startService(Intent(this, LogoutService::class.java))
                // If logged in, we need to check the role in Firestore
                checkUserRoleAndNavigate(currentUser.uid)
            } else {
                // Otherwise, go to introduction/login
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 2500)
    }

    private fun checkUserRoleAndNavigate(uid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role") ?: "Student"
                    val name = document.getString("fullName") ?: ""
                    
                    val intent = if (role == "Admin") {
                        Intent(this, AdminDashboard::class.java)
                    } else {
                        Intent(this, StudentDashboard::class.java)
                    }
                    intent.putExtra("USER_NAME", name)
                    startActivity(intent)
                } else {
                    // If user record doesn't exist, go to login
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                // On failure, default to login
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
    }
}