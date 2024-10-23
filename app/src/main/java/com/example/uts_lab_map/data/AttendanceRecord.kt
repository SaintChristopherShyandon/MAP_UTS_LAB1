package com.example.uts_lab_map.data

data class AttendanceRecord(
    val day: String,
    val date: String,
    var time: String,
    var status: String,
    var photo: String,
    var isCheckedIn: Boolean = false, // Status untuk absen masuk
    var isCheckedOut: Boolean = false // Status untuk absen pulang
)


