package com.example.uts_lab_map

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextNIM: EditText
    private lateinit var buttonSave: Button
    private lateinit var iconBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Inisialisasi elemen UI
        iconBack = findViewById(R.id.iconBack)
        editTextName = findViewById(R.id.editTextName)
        editTextNIM = findViewById(R.id.editTextNIM)
        buttonSave = findViewById(R.id.buttonSave)

        // Fungsi back saat ikon diklik
        iconBack.setOnClickListener {
            finish() // Menutup EditProfileActivity
        }

        // Ambil data yang ada sebelumnya dari intent
        val userName = intent.getStringExtra("USER_NAME") ?: ""
        val userNIM = intent.getStringExtra("USER_NIM") ?: ""

        // Tampilkan data yang ada di EditText
        editTextName.setText(userName)
        editTextNIM.setText(userNIM)

        // Set listener untuk tombol simpan
        buttonSave.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("NEW_USER_NAME", editTextName.text.toString())
            resultIntent.putExtra("NEW_USER_NIM", editTextNIM.text.toString())
            setResult(Activity.RESULT_OK, resultIntent)
            finish() // Kembali ke ProfileActivity
        }
    }
}
