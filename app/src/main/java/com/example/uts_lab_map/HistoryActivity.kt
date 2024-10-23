package com.example.uts_lab_map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uts_lab_map.data.AttendanceRecord
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class HistoryActivity : AppCompatActivity() {

    private val attendanceRecords = mutableListOf<AttendanceRecord>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AttendanceRecordAdapter
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialize Firestore, FirebaseAuth, FirebaseStorage, and RecyclerView
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AttendanceRecordAdapter(attendanceRecords)
        recyclerView.adapter = adapter

        // Set up bottom navigation
        setupBottomNavigation()

        // Check if a user is signed in and fetch attendance records
        if (auth.currentUser != null) {
            fetchAttendanceRecords()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Retrieve AttendanceRecord from Intent
        intent.getSerializableExtra("attendance_record")?.let { record ->
            val attendanceRecord = record as AttendanceRecord
            attendanceRecords.add(0, attendanceRecord) // Add new record at the top
            saveAttendanceRecordToFirestore(attendanceRecord)
            adapter.notifyItemInserted(0) // Notify adapter of new item
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.action_history
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_history -> true
                R.id.action_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.action_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchAttendanceRecords() {
        firestore.collection("attendanceRecords")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { result ->
                attendanceRecords.clear() // Clear list before filling
                for (document in result) {
                    val record = document.toObject(AttendanceRecord::class.java)
                    attendanceRecords.add(record)
                }
                attendanceRecords.sortByDescending { it.date }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                println("Error fetching documents: $e")
            }
    }

    private fun saveAttendanceRecordToFirestore(record: AttendanceRecord) {
        val user = auth.currentUser
        if (record.photo.isNotEmpty()) {
            // Upload photo to Firebase Storage and then save record
            uploadPhotoToStorage(record.photo) { photoUrl ->
                val recordMap = hashMapOf(
                    "userId" to user?.uid,
                    "date" to record.date,
                    "time" to record.time,
                    "status" to record.status,
                    "photo" to photoUrl,
                    "isCheckedIn" to record.isCheckedIn,
                    "isCheckedOut" to record.isCheckedOut
                )

                firestore.collection("attendanceRecords")
                    .add(recordMap)
                    .addOnSuccessListener { documentReference ->
                        println("Attendance record saved with ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        println("Error adding document: $e")
                    }
            }
        }
    }

    private fun uploadPhotoToStorage(photoPath: String, callback: (photoUrl: String) -> Unit) {
        val storageRef = storage.reference.child("attendancePhotos/${System.currentTimeMillis()}.jpg")
        val photoUri = Uri.parse(photoPath)

        storageRef.putFile(photoUri)
            .addOnSuccessListener {
                // Get the URL of the uploaded file
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                println("Error uploading photo: $e")
                callback("") // Return empty string if upload fails
            }
    }
}
