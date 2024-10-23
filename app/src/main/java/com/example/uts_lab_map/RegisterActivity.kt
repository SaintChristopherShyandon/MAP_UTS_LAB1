package com.example.uts_lab_map

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // References to UI elements
        val emailEditText: EditText = findViewById(R.id.editTextTextEmailAddress2)
        val passwordEditText: EditText = findViewById(R.id.editTextTextPassword2)
        val confirmPasswordEditText: EditText = findViewById(R.id.editTextTextPassword3)
        val registerButton: Button = findViewById(R.id.registbutton)
        val loginTextView: TextView = findViewById(R.id.textView9) // Reference to "Login" TextView

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Validate input
            if (email.isEmpty()) {
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else if (password.isEmpty()) {
                Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else if (confirmPassword.isEmpty()) {
                Toast.makeText(this, "Konfirmasi password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Password dan konfirmasi password tidak sama", Toast.LENGTH_SHORT).show()
            } else {
                // Register user with Firebase
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Pendaftaran berhasil untuk $email", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Pendaftaran gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // Navigate to LoginActivity when "Login" TextView is clicked
        loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Optional: closes RegisterActivity to prevent going back to it
        }
    }
}
