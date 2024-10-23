package com.example.uts_lab_map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.uts_lab_map.data.AttendanceRecord
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var iconTouch: ImageView
    private lateinit var imageViewPreview: ImageView
    private lateinit var buttonRetake: Button
    private lateinit var buttonUpload: Button
    private lateinit var circleBackground: ImageView
    private var photoBitmap: Bitmap? = null

    companion object {
        const val CAMERA_PERMISSION_CODE = 100
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi elemen UI
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val textViewDateTime = findViewById<TextView>(R.id.textViewDateTime)
        iconTouch = findViewById(R.id.icon_touch)
        imageViewPreview = findViewById(R.id.imageViewPreview)
        buttonRetake = findViewById(R.id.buttonRetake)
        buttonUpload = findViewById(R.id.buttonUpload)
        circleBackground = findViewById(R.id.imageView7) // Inisialisasi circle_background

        // Display current date and time
        val currentDateTime = SimpleDateFormat("EEEE, dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        textViewDateTime.text = currentDateTime

        // Set bottom navigation default selection to Home
        bottomNavigationView.selectedItemId = R.id.action_home

        // Set up a listener to handle navigation item clicks
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_history -> {
                    // Pindah ke HistoryActivity
                    val historyIntent = Intent(this, HistoryActivity::class.java)
                    startActivity(historyIntent)
                    true
                }
                R.id.action_profile -> {
                    // Pindah ke ProfileActivity
                    val profileIntent = Intent(this, ProfileActivity::class.java)
                    startActivity(profileIntent)
                    true
                }
                else -> false
            }
        }

        // Set up click listener for icon_touch to open the camera
        iconTouch.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
        }

        // Set up button listeners
        buttonRetake.setOnClickListener { checkCameraPermissionAndOpenCamera() }
        buttonUpload.setOnClickListener { uploadPhoto() }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val photo = result.data?.extras?.get("data") as? Bitmap
            if (photo != null) {
                photoBitmap = photo

                // Menampilkan pratinjau foto dan tombol
                imageViewPreview.setImageBitmap(photo)
                imageViewPreview.visibility = View.VISIBLE
                buttonRetake.visibility = View.VISIBLE
                buttonUpload.visibility = View.VISIBLE

                // Menyembunyikan icon touch dan circle background setelah foto berhasil diambil
                iconTouch.visibility = View.GONE
                circleBackground.visibility = View.GONE
            } else {
                Toast.makeText(this, "Gagal mengambil foto", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Foto gagal diambil", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadPhoto() {
        if (photoBitmap != null) {
            // Dapatkan tanggal hari ini
            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val currentDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())

            // Cek apakah pengguna sudah melakukan absen hari ini
            val sharedPreferences = getSharedPreferences("attendancePrefs", Context.MODE_PRIVATE)
            val gson = Gson()
            val json = sharedPreferences.getString("attendance_records", null)

            // Konversi catatan yang ada menjadi daftar mutable
            val type = object : TypeToken<MutableList<AttendanceRecord>>() {}.type
            val attendanceRecords: MutableList<AttendanceRecord> = if (json != null) {
                gson.fromJson(json, type) ?: mutableListOf()
            } else {
                mutableListOf()
            }

            // Cek status absen masuk dan pulang
            val existingRecord = attendanceRecords.find { it.date == currentDate }
            if (existingRecord != null) {
                if (!existingRecord.isCheckedIn) {
                    // Jika belum absen masuk, lakukan absen masuk
                    existingRecord.isCheckedIn = true
                    existingRecord.time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    existingRecord.status = "Present"
                    existingRecord.photo = saveBitmapToFile(photoBitmap!!)
                    Toast.makeText(this, "Absen masuk berhasil", Toast.LENGTH_SHORT).show()
                } else if (!existingRecord.isCheckedOut) {
                    // Jika sudah absen masuk tapi belum absen pulang
                    existingRecord.isCheckedOut = true
                    existingRecord.time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    existingRecord.status = "Checked Out"
                    existingRecord.photo = saveBitmapToFile(photoBitmap!!)
                    Toast.makeText(this, "Absen pulang berhasil", Toast.LENGTH_SHORT).show()
                } else {
                    // Jika sudah absen pulang
                    Toast.makeText(this, "Anda sudah melakukan absen hari ini", Toast.LENGTH_SHORT).show()
                    return
                }
            } else {
                // Jika tidak ada catatan untuk hari ini, buat catatan baru
                val attendanceRecord = AttendanceRecord(
                    day = currentDay,
                    date = currentDate,
                    time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                    status = "Present",
                    photo = saveBitmapToFile(photoBitmap!!),
                    isCheckedIn = true // Set status absen masuk
                )
                attendanceRecords.add(attendanceRecord)
                Toast.makeText(this, "Absen masuk berhasil", Toast.LENGTH_SHORT).show()
            }

            // Simpan catatan ke SharedPreferences
            saveAttendanceRecordToPreferences(attendanceRecords)

            // Reset UI
            resetUI()
        } else {
            Toast.makeText(this, "Tidak ada foto untuk diunggah", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveAttendanceRecordToPreferences(attendanceRecords: MutableList<AttendanceRecord>) {
        val sharedPreferences = getSharedPreferences("attendancePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        editor.putString("attendance_records", gson.toJson(attendanceRecords))
        editor.apply()
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val file = File(cacheDir, "attendance_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file.absolutePath // Return the file path
    }

    private fun resetUI() {
        imageViewPreview.visibility = View.GONE
        buttonRetake.visibility = View.GONE
        buttonUpload.visibility = View.GONE
        iconTouch.visibility = View.VISIBLE
        circleBackground.visibility = View.VISIBLE // Tampilkan lagi circle_background
        photoBitmap = null // Reset the photo bitmap
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
            }
            }
        }
}