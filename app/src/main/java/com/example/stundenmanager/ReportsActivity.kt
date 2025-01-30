package com.example.stundenmanager

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stundenmanager.absences.AbsencesActivity
import com.example.stundenmanager.workhours.WorkHoursActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table

class ReportsActivity : AppCompatActivity() {

    private lateinit var spinnerReportPeriod: Spinner
    private lateinit var btnGenerateReport: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnExportPDF: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reports)

        // highlight menuIcon
        MainActivity.highlightActiveMenu(this, R.id.menu_reports)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Menu-Button-Listener
        setupMenuNavigation()

        spinnerReportPeriod = findViewById(R.id.spinnerReportPeriod)
        btnGenerateReport = findViewById(R.id.btnGenerateReport)
        recyclerView = findViewById(R.id.recyclerReport)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        btnExportPDF = findViewById(R.id.btnExportPDF)

        btnGenerateReport.setOnClickListener {
            generateReport()
        }

        btnExportPDF.setOnClickListener {
            val adapter = recyclerView.adapter as? ReportAdapter
            if (adapter != null && adapter.itemCount > 0) {
                exportReportToPDF(adapter.getReportData())
            } else {
                Log.d("ReportsActivity.kt", "No report available for export.")
                Toast.makeText(this, getString(R.string.report_no_available_export), Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SimpleDateFormat")
    private fun generateReport() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        val selectedPeriod = spinnerReportPeriod.selectedItem.toString()
        val (startDate, endDate) = getDateRange(selectedPeriod)

        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { userDoc ->
                    val shift = userDoc.getString("shift") ?: "unknown"

                    FirebaseFirestore.getInstance().collection("users")
                        .document(userId)
                        .collection("workHours")
                        .whereGreaterThanOrEqualTo("startTime", Timestamp(SimpleDateFormat("yyyy-MM-dd").parse(startDate)!!))
                        .whereLessThanOrEqualTo("endTime", Timestamp(SimpleDateFormat("yyyy-MM-dd").parse(endDate)!!))
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (querySnapshot.isEmpty) {
                                Log.d("ReportsActivity.kt", "No data found for the selected period.")
                                Toast.makeText(this, getString(R.string.report_no_data), Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }
                            Log.d("ReportsActivity", "Loaded entries: ${querySnapshot.size()}")

                            val reportItems = mutableListOf<ReportItem>()

                            for (doc in querySnapshot.documents) {
                                val startTime = doc.getTimestamp("startTime")?.toDate()
                                val endTime = doc.getTimestamp("endTime")?.toDate()
                                val breakDuration = doc.getLong("breakDuration") ?: 0

                                val plannedShiftTimes = getPlannedShiftTimes(shift)

                                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                                val formattedDate = dateFormat.format(startTime!!)
                                val formattedStartTime = timeFormat.format(startTime!!)
                                val formattedEndTime = timeFormat.format(endTime!!)

                                reportItems.add(
                                    ReportItem(
                                        formattedDate,  // Display date only
                                        "${plannedShiftTimes.first} - ${plannedShiftTimes.second}",  // Scheduled times
                                        "$formattedStartTime - $formattedEndTime",  // Display actual time only
                                        "$breakDuration min"
                                    )
                                )

                            }

                            // Update RecyclerView
                            recyclerView = findViewById(R.id.recyclerReport)
                            recyclerView.layoutManager = LinearLayoutManager(this)
                            recyclerView.setHasFixedSize(true)
                            recyclerView.adapter = ReportAdapter(reportItems)
                            recyclerView.adapter?.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, getString(R.string.report_recycle_fail) + e.message, Toast.LENGTH_SHORT).show()
                        }
                }
        }
    }

    private fun getPlannedShiftTimes(shift: String): Pair<String, String> {
        return when (shift.lowercase(Locale.getDefault())) {
            "morning" -> Pair("06:00", "14:00")
            "evening" -> Pair("14:00", "22:00")
            else -> Pair("-", "-")  // If shift is not found
        }
    }

    private fun getDateRange(period: String): Pair<String, String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val endDate = dateFormat.format(calendar.time)

        when (period) {
            "Last 7 days" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            "Last month" -> calendar.add(Calendar.MONTH, -1)
            "Last year" -> calendar.add(Calendar.YEAR, -1)
            else -> {
                Toast.makeText(this, getString(R.string.report_time_unknown) + period, Toast.LENGTH_SHORT).show()
                Log.e("ReportsActivity", "getDateRange: unknown period $period")
                return Pair("", "")  // Return empty values to avoid errors
            }
        }

        val startDate = dateFormat.format(calendar.time)
        return Pair(startDate, endDate)
    }

    private fun exportReportToPDF(reportData: String) {
        try {
            val filePath = getExternalFilesDir(null)?.absolutePath + "/report.pdf"
            val pdfWriter = PdfWriter(filePath)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Add a title heading
            document.add(Paragraph(getString(R.string.work_time_report)).setBold().setFontSize(18f))

            val table = Table(floatArrayOf(2f, 3f, 3f, 2f))
            // Heading for the table
            table.addCell(Cell().add(Paragraph(getString(R.string.date)).setBold()))
            table.addCell(Cell().add(Paragraph(getString(R.string.planned)).setBold()))
            table.addCell(Cell().add(Paragraph(getString(R.string.actually)).setBold()))
            table.addCell(Cell().add(Paragraph(getString(R.string.workbreak)).setBold()))

            // Add the report data
            for (line in reportData.lines()) {
                line.split("|").forEach {
                    table.addCell(Cell().add(Paragraph(it.trim())))
                }
            }
            document.add(table)

            document.close()

            Toast.makeText(this, getString(R.string.export_report_pdf_saved) + filePath, Toast.LENGTH_LONG).show()
            Log.d("ReportsActivity", "PDF saved successfully: $filePath")

        } catch (e: Exception) {
            Log.e("ReportsActivity", "Error during PDF export: ${e.message}")
            Toast.makeText(this, getString(R.string.export_report_pdf_err) + e.message, Toast.LENGTH_LONG).show()
        }
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
            Toast.makeText(this, "Du bist bereits auf den Berichten-Seite", Toast.LENGTH_SHORT).show()
        }
        findViewById<ImageButton>(R.id.menu_statistics).setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
    }
}
