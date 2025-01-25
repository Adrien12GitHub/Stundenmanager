package com.example.stundenmanager

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.stundenmanager.absences.AbsencesActivity
import com.example.stundenmanager.workhours.WorkHoursActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.TimeZone

data class StatisticsData(
    val totalWorkHours: Float,
    val totalAbsence: Float,
    val totalBreaks: Float
)

class StatisticsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        pieChart = findViewById(R.id.pieChart)
        setupPieChart()
        setupMenuNavigation()
        setupTimeFilter()
    }

    private fun setupMenuNavigation() {
        findViewById<ImageButton>(R.id.menu_home).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        findViewById<ImageButton>(R.id.menu_workhours).setOnClickListener {
            startActivity(Intent(this, WorkHoursActivity::class.java))
        }
        findViewById<ImageButton>(R.id.menu_absences).setOnClickListener {
            startActivity(Intent(this, AbsencesActivity::class.java))
        }
        findViewById<ImageButton>(R.id.menu_messages).setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }
        findViewById<ImageButton>(R.id.menu_reports).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        findViewById<ImageButton>(R.id.menu_statistics).setOnClickListener {
            Toast.makeText(this, "You are already on the Statistics page", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTimeFilter() {
        val spinner: Spinner = findViewById(R.id.time_filter_spinner)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val calendar = Calendar.getInstance()
                calendar.timeZone = TimeZone.getTimeZone("UTC")
                val endDate = calendar.timeInMillis

                when (position) {
                    0 -> calendar.add(Calendar.DAY_OF_YEAR, -7) // Last 7 days
                    1 -> calendar.add(Calendar.MONTH, -1) // Last month
                    2 -> calendar.add(Calendar.YEAR, -1) // Last year
                }

                val startDate = calendar.timeInMillis
                Log.d("Statistics Firestore", "Query period: Start = $startDate, End = $endDate")
                loadStatisticsFromFirestore(startDate, endDate)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadStatisticsFromFirestore(startDate: Long, endDate: Long) {
        var totalWorkHours = 0.0
        var totalAbsenceHours = 0.0
        var totalBreaks = 0.0

        val userId = Firebase.auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("workHours")
            .whereGreaterThanOrEqualTo("startTime", Timestamp(startDate / 1000, 0))
            .whereLessThanOrEqualTo("endTime", Timestamp(endDate / 1000, 0))
            .get()
            .addOnSuccessListener { workHoursSnapshot ->
                if (workHoursSnapshot.isEmpty) {
                    Log.d("Statistics Firestore", "No WorkHours data found in the period")
                }
                for (doc in workHoursSnapshot.documents) {
                    Log.d("Statistics Firestore", "Document data: ${doc.data}")
                    val workDuration = doc.getDouble("hoursWorked") ?: 0.0
                    val breakDurationMinutes = doc.getDouble("breakDuration") ?: 0.0
                    val breakDurationHours = breakDurationMinutes / 60  // Conversion to hours
                    totalWorkHours += workDuration
                    totalBreaks += breakDurationHours
                }
                db.collection("users")
                    .document(userId)
                    .collection("absences")
                    .whereGreaterThanOrEqualTo("dateFrom", Timestamp(startDate / 1000, 0))
                    .whereLessThanOrEqualTo("dateTo", Timestamp(endDate / 1000, 0))
                    .get()
                    .addOnSuccessListener { absencesSnapshot ->
                        if (absencesSnapshot.isEmpty) {
                            Log.d("Statistics Firestore", "No absences in the selected period")
                        }
                        for (doc in absencesSnapshot.documents) {
                            Log.d("Statistics Firestore", "Absence document: ${doc.data}")
                            val dateFrom = doc.getTimestamp("dateFrom")?.toDate()
                            val dateTo = doc.getTimestamp("dateTo")?.toDate()
                            var absenceDuration = 0.0

                            if (dateFrom != null && dateTo != null) {
                                val diff = dateTo.time - dateFrom.time
                                absenceDuration = (diff / (1000 * 60 * 60)).toDouble()  // Conversion to hours
                            }

                            totalAbsenceHours += absenceDuration
                        }
                        if (totalAbsenceHours == 0.0) {
                            Toast.makeText(this, getString(R.string.no_absences_in_period), Toast.LENGTH_SHORT).show()
                        }
                        Log.d("Statistics Firestore Debug", "Total Work: $totalWorkHours, Absences: $totalAbsenceHours, Breaks: $totalBreaks")
                        val statisticsData = StatisticsData(
                            totalWorkHours.toFloat(),
                            totalAbsenceHours.toFloat(),
                            totalBreaks.toFloat()
                        )
                        updatePieChart(statisticsData)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Statistics Firestore", "Error fetching absences: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Statistics Firestore", "Error fetching work hours: ${e.message}")
            }
    }

    @SuppressLint("DefaultLocale")
    private fun updatePieChart(statisticsData: StatisticsData) {
        val entries = ArrayList<PieEntry>().apply {
            if (statisticsData.totalWorkHours > 0)
                add(PieEntry(String.format("%.2f", statisticsData.totalWorkHours).toDouble().toFloat(), "Work Hours"))
            if (statisticsData.totalBreaks > 0)
                add(PieEntry(String.format("%.2f", statisticsData.totalBreaks).toDouble().toFloat(), "Breaks"))
            if (statisticsData.totalAbsence > 0)
                add(PieEntry(String.format("%.2f", statisticsData.totalAbsence).toDouble().toFloat(), "Absences"))
        }

        if (entries.isEmpty()) {
            Log.d("Statistics Firestore", "piechart entries is empty")
            Toast.makeText(this, getString(R.string.no_data_available), Toast.LENGTH_SHORT).show()
            pieChart.setNoDataText(getString(R.string.no_data_in_period))
            pieChart.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "Statistics").apply {
            colors = listOf(
                Color.parseColor("#FF5722"), // Work Hours
                Color.parseColor("#03A9F4"), // Absences
                Color.parseColor("#8BC34A")  // Breaks
            )
            valueTextSize = 16f
            valueTextColor = Color.BLACK
            // Set a user-defined formatter for the values
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.2f h", value)
                }
            }
        }
        // Creating the PieData and adding it to the diagram
        pieChart.data = PieData(dataSet)
        pieChart.invalidate()
    }

    private fun setupPieChart() {
        pieChart.description.isEnabled = false
        pieChart.isRotationEnabled = true
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelTextSize(14f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.centerText = getString(R.string.overview_work)
        pieChart.setCenterTextSize(20f)
    }

}


