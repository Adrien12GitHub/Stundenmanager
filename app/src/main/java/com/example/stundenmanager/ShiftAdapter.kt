package com.example.stundenmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class ShiftAdapter(private val shifts: List<Shift>) : RecyclerView.Adapter<ShiftAdapter.ShiftViewHolder>() {

    class ShiftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserId: TextView = itemView.findViewById(R.id.tvUserId)
        val tvShiftTime: TextView = itemView.findViewById(R.id.tvShiftTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShiftViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shift, parent, false)
        return ShiftViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShiftViewHolder, position: Int) {
        val shift = shifts[position]
        val dateFormat = SimpleDateFormat("EEE dd MMM, HH:mm", Locale.getDefault())
        holder.tvUserId.text = "Employee ID: ${shift.userId}"
        holder.tvShiftTime.text = "Start: ${dateFormat.format(shift.startTime)}\nEnd: ${dateFormat.format(shift.endTime)}"
    }

    override fun getItemCount() = shifts.size
}