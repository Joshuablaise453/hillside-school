package com.electrohub.schoolmanagementapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val mainView = findViewById<android.view.View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
            applyBounceToAllClickables(mainView)
        }

        setupDropdowns()

        findViewById<TextView>(R.id.loginText)?.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.signupButton)?.setOnClickListener {
            val signupButton = it as Button
            performSignup(signupButton)
        }
    }

    private fun performSignup(signupButton: Button) {
        val fullName = findViewById<EditText>(R.id.fullNameInput)?.text.toString().trim()
        val email = findViewById<EditText>(R.id.emailInput)?.text.toString().trim()
        val gender = findViewById<AutoCompleteTextView>(R.id.genderDropdown)?.text.toString()
        val role = findViewById<AutoCompleteTextView>(R.id.roleDropdown)?.text.toString()
        val phone = findViewById<EditText>(R.id.phoneInput)?.text.toString().trim()
        val password = findViewById<EditText>(R.id.passwordInput)?.text.toString()
        val confirmPassword = findViewById<EditText>(R.id.confirmPasswordInput)?.text.toString()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        signupButton.text = "Signing up..."
        signupButton.isEnabled = false
        
        auth.createUserWithEmailAndPassword(email, password!!)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        saveUserToFirestore(userId, fullName, email, gender, role, phone, signupButton)
                    }
                } else {
                    signupButton.text = "Sign Up"
                    signupButton.isEnabled = true
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToFirestore(uid: String, name: String, email: String, gender: String, role: String, phone: String, signupButton: Button) {
        val userMap = hashMapOf(
            "uid" to uid,
            "fullName" to name,
            "email" to email,
            "gender" to gender,
            "role" to role,
            "phone" to phone,
            "registered" to true
        )

        db.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Account Registered Successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                signupButton.text = "Sign Up"
                signupButton.isEnabled = true
                Toast.makeText(this, "Failed to save profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupDropdowns() {
        val genders = arrayOf("Male", "Female", "Other")
        val genderDropdown = findViewById<AutoCompleteTextView>(R.id.genderDropdown)
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        genderDropdown?.setAdapter(genderAdapter)
        genderDropdown?.setOnClickListener { genderDropdown.showDropDown() }

        val roles = arrayOf("Student", "Parent", "Teacher", "Admin")
        val roleDropdown = findViewById<AutoCompleteTextView>(R.id.roleDropdown)
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        roleDropdown?.setAdapter(roleAdapter)
        roleDropdown?.setOnClickListener { roleDropdown.showDropDown() }

        val countryCodes = listOf("+256 (UG)", "+254 (KE)", "+255 (TZ)", "+250 (RW)")
        val countryDropdown = findViewById<AutoCompleteTextView>(R.id.countryCodeDropdown)
        val countryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, countryCodes)
        countryDropdown?.setAdapter(countryAdapter)
        countryDropdown?.setText(countryCodes[0], false)
        countryDropdown?.setOnClickListener { countryDropdown.showDropDown() }
    }
}