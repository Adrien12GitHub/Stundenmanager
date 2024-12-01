package com.example.stundenmanager.workhours

import com.example.stundenmanager.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.Timestamp

data class WorkHour(
    val startTime: Timestamp,
    val endTime: Timestamp,
    val breakDuration: Int,
    val hoursWorked: Double,
    val comment: String
)

class WorkHoursAdapter(private val workHoursList: List<WorkHour>) :
    RecyclerView.Adapter<WorkHoursAdapter.WorkHoursViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkHoursViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_work_hour, parent, false)
        return WorkHoursViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkHoursViewHolder, position: Int) {
        val workHour = workHoursList[position]
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val startTime = workHour.startTime.toDate()
        val endTime = workHour.endTime.toDate()

        val context = holder.itemView.context

        holder.workDate.text = context.getString(R.string.item_date, dateFormat.format(startTime)) //"Datum: ${dateFormat.format(startTime)}"
        holder.workTime.text = context.getString(R.string.item_period, timeFormat.format(startTime), timeFormat.format(endTime)) //"Zeitraum: ${timeFormat.format(startTime)} - ${timeFormat.format(endTime)}"
        holder.breakDuration.text = context.getString(R.string.item_pause, workHour.breakDuration) //"Pause: ${workHour.breakDuration} Minuten"
        holder.hoursWorked.text = context.getString(R.string.item_worked_hours, workHour.hoursWorked) //"Gearbeitete Stunden: ${workHour.hoursWorked}"
        holder.comment.text =  context.getString(R.string.item_comment, workHour.comment) //"Kommentar: ${workHour.comment}"
    }

    override fun getItemCount(): Int = workHoursList.size

    class WorkHoursViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workDate: TextView = itemView.findViewById(R.id.tvWorkDate)
        val workTime: TextView = itemView.findViewById(R.id.tvWorkTime)
        val breakDuration: TextView = itemView.findViewById(R.id.tvBreak)
        val hoursWorked: TextView = itemView.findViewById(R.id.tvHoursWorked)
        val comment: TextView = itemView.findViewById(R.id.tvComment)
    }
}