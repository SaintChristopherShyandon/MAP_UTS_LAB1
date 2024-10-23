package com.example.uts_lab_map

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.uts_lab_map.data.AttendanceRecord

class AttendanceRecordAdapter(
    private val attendanceRecords: List<AttendanceRecord>
) : RecyclerView.Adapter<AttendanceRecordAdapter.AttendanceViewHolder>() {

    class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewDay: TextView = itemView.findViewById(R.id.textViewDay)
        val imageViewPhoto: ImageView = itemView.findViewById(R.id.imageViewPhoto)
        val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_record, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val record = attendanceRecords[position]
        holder.textViewDay.text = "Hari: ${record.day}"
        holder.textViewDate.text = "Tanggal: ${record.date}"
        holder.textViewTime.text = "Jam: ${record.time}"
        holder.textViewStatus.text = "Status: ${record.status}"

        // Tampilkan foto absen dari path
        val photoBitmap = BitmapFactory.decodeFile(record.photo)
        holder.imageViewPhoto.setImageBitmap(photoBitmap)
    }

    override fun getItemCount(): Int {
        return attendanceRecords.size
    }
}


