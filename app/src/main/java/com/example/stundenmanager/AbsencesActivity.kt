package com.example.stundenmanager

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class AbsencesActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var reasonSpinner: Spinner
    private lateinit var editTextDateFrom: EditText
    private lateinit var editTextDateTo: EditText
    private lateinit var uploadButton: Button
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absences)

        auth = FirebaseAuth.getInstance()

        reasonSpinner = findViewById(R.id.reasonSpinner)
        editTextDateFrom = findViewById(R.id.editTextDateFrom)
        editTextDateTo = findViewById(R.id.editTextDateTo)
        uploadButton = findViewById(R.id.uploadButton)
        submitButton = findViewById(R.id.sendButton)

        // Set up DatePicker for date fields
        setUpDatePicker(editTextDateFrom)
        setUpDatePicker(editTextDateTo)

        // Set up Upload Button
        uploadButton.setOnClickListener {
            // allow the user to select a file from their device
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(intent, 1)
        }

        // Set up Send Button
        submitButton.setOnClickListener {
            if (validateForm()) {
                Toast.makeText(this, "Form submitted successfully!", Toast.LENGTH_SHORT).show()
            }
        }

        // Menü-Button-Listener
        setupMenuNavigation()
    }

    private fun validateForm(): Boolean {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Calendar.getInstance().time

        try {
            val dateFrom = sdf.parse(editTextDateFrom.text.toString())
            val dateTo = sdf.parse(editTextDateTo.text.toString())

            if (dateFrom.before(currentDate) || dateTo.before(currentDate)) {
                Toast.makeText(this, "Datum darf nicht in der Vergangenheit liegen", Toast.LENGTH_SHORT).show()
                return false
            }
            if (dateFrom.after(dateTo)) {
                Toast.makeText(this, "Enddatum darf nicht vor dem Startdatum liegen", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ungültiges Datum", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun setUpDatePicker(editText: EditText) {
        editText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val date = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                editText.setText(date)
            }, year, month, day)
            datePickerDialog.show()
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
            Toast.makeText(this, "Du bist bereits auf der Abwesenheitsseite", Toast.LENGTH_SHORT).show()
        }
        findViewById<ImageButton>(R.id.menu_messages).setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }
        findViewById<ImageButton>(R.id.menu_reports).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        findViewById<ImageButton>(R.id.menu_statistics).setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
    }
}