package com.example.uts_lab_map

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var textViewProfile: TextView
    private lateinit var buttonEditProfile: Button
    private lateinit var buttonLogout: ImageView  // Change Button to ImageView
    private val EDIT_PROFILE_REQUEST = 1
    private val firestore = FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize UI elements
        textViewProfile = findViewById(R.id.textViewProfile)
        buttonEditProfile = findViewById(R.id.buttonEditProfile)
        buttonLogout = findViewById(R.id.buttonLogout) // Cast as ImageView

        // Load user profile from Firebase
        loadUserProfile()

        // Setup Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.action_profile

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    finish()
                    true
                }
                R.id.action_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.action_profile -> true
                else -> false
            }
        }

        // Edit Profile button
        buttonEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivityForResult(intent, EDIT_PROFILE_REQUEST)
        }

        // Logout button
        buttonLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserProfile() {
        firestore.collection("users").document("profileData")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userName = document.getString("name") ?: "Nama Pengguna"
                    val userNIM = document.getString("nim") ?: "123456789"
                    textViewProfile.text = "Profile:\n\nNama: $userName\nNIM: $userNIM"
                } else {
                    Toast.makeText(this, "Document does not exist.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading profile: $e", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == RESULT_OK) {
            val newUserName = data?.getStringExtra("NEW_USER_NAME")
            val newUserNIM = data?.getStringExtra("NEW_USER_NIM")

            // Save updated data to Firebase
            val profileData = hashMapOf(
                "name" to newUserName,
                "nim" to newUserNIM
            )

            firestore.collection("users").document("profileData")
                .set(profileData)
                .addOnSuccessListener {
                    // Update profile view
                    textViewProfile.text = "Profile:\n\nNama: $newUserName\nNIM: $newUserNIM"
                }
                .addOnFailureListener { e ->
                    println("Error saving profile: $e")
                }
        }
    }
}
