package com.example.stundenmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportAdapter(private val reportList: List<ReportItem>) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val item = reportList[position]

        holder.date.text = item.date
        holder.planned.text = item.planned
        holder.actual.text = item.actual
        holder.breaks.text = item.breakDuration

        // Set the view size to ensure that the entries are displayed correctly
        holder.itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun getItemCount(): Int = reportList.size

    class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.txtDate)
        val planned: TextView = view.findViewById(R.id.txtPlanned)
        val actual: TextView = view.findViewById(R.id.txtActual)
        val breaks: TextView = view.findViewById(R.id.txtBreaks)
    }

    fun getReportData(): String {
        val builder = StringBuilder()
        for (item in reportList) {
            builder.append("${item.date} | ${item.planned} | ${item.actual} | ${item.breakDuration}\n")
        }
        return builder.toString()
    }

    fun getReports(): List<ReportItem> {
        return reportList
    }

}

// Data model for the report entries
data class ReportItem(
    val date: String,
    val planned: String,
    val actual: String,
    val breakDuration: String
)


