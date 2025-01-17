package com.example.stundenmanager.absences

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stundenmanager.R
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.Timestamp

data class Absence(
    val reason: String,
    val dateFrom: Timestamp,
    val dateTo: Timestamp)

class AbsencesAdapter(private var absencesList: List<Absence>) : RecyclerView.Adapter<AbsencesAdapter.AbsenceViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsenceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_absence, parent, false)
        return AbsenceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsenceViewHolder, position: Int) {
        val absence = absencesList[position]
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val dateFrom = absence.dateFrom.toDate()
        val dateTo = absence.dateTo.toDate()

        holder.reasonTextView.text = absence.reason
        holder.dateFromTextView.text = "Von: ${dateFormat.format(dateFrom)}"
        holder.dateToTextView.text = "Bis: ${dateFormat.format(dateTo)}"
    }

    override fun getItemCount(): Int = absencesList.size

    class AbsenceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateFromTextView: TextView = itemView.findViewById(R.id.dateFromTextView)
        val dateToTextView: TextView = itemView.findViewById(R.id.dateToTextView)
        val reasonTextView: TextView = itemView.findViewById(R.id.reasonTextView)
    }
}