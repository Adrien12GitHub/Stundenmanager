package com.example.stundenmanager

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stundenmanager.workhours.WorkHoursActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class AbsencesActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var reasonSpinner: Spinner
    private lateinit var editTextDateFrom: EditText
    private lateinit var editTextDateTo: EditText
    private lateinit var uploadButton: Button
    private lateinit var submitButton: Button
    private lateinit var selectedFileUri: Uri
    private lateinit var filePreview: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absences)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        reasonSpinner = findViewById(R.id.reasonSpinner)
        editTextDateFrom = findViewById(R.id.editTextDateFrom)
        editTextDateTo = findViewById(R.id.editTextDateTo)
        uploadButton = findViewById(R.id.uploadButton)
        submitButton = findViewById(R.id.sendButton)
        filePreview = findViewById(R.id.filePreview)

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
                val reason = reasonSpinner.selectedItem.toString()
                val dateFrom = Timestamp(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(editTextDateFrom.text.toString()))
                val dateTo = Timestamp(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(editTextDateTo.text.toString()))
                val file = selectedFileUri

                saveAbsences(reason, dateFrom, dateTo, file)

                // clear the form
                reasonSpinner.setSelection(0)
                editTextDateFrom.text.clear()
                editTextDateTo.text.clear()
                filePreview.text = "Ausgewählte Datei: Keine"
            }
        }

        // Fetch and display absences
        fetchAndDisplayAbsences()

        // Menü-Button-Listener
        setupMenuNavigation()
    }

    private fun validateForm(): Boolean {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

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
        if (filePreview.text == "Ausgewählte Datei: Keine") {
            Toast.makeText(this, "Bitte eine Datei auswählen", Toast.LENGTH_SHORT).show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                filePreview.text = "Ausgewählte Datei: ${uri.path}"
            }
        }
    }

    private fun saveAbsences(
        reason: String,
        dateFrom: Timestamp,
        dateTo: Timestamp,
        file: Uri
    ) {
        Log.d("AbsencesActivity.kt", "saveAbsences")
        val userId = auth.currentUser?.uid ?: return
        val absenceData = hashMapOf(
            "reason" to reason,
            "dateFrom" to dateFrom,
            "dateTo" to dateTo,
            "fileUri" to file
        )

        db.collection("users").document(userId).collection("absences")
            .add(absenceData)
            .addOnSuccessListener {
                Log.d("AbsencesActivity.kt", "saveAbsences: addOnSuccessListener")
                Toast.makeText(this, "Abwesenheiten erfolgreich gespeichert", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.d("AbsencesActivity.kt", "saveAbsences: addOnFailureListener")
                Toast.makeText(this, "Fehler beim Speichern der Abwesenheiten", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchAndDisplayAbsences() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("absences")
            .get().addOnSuccessListener { documents ->
                val absencesList = mutableListOf<Absence>()
                for (document in documents) {
                    val reason = document.getString("reason") ?: ""
                    val dateFrom = document.getTimestamp("dateFrom") ?: continue
                    val dateTo = document.getTimestamp("dateTo") ?: continue

                    absencesList.add(
                        Absence(
                            reason = reason,
                            dateFrom = dateFrom,
                            dateTo = dateTo
                        )
                    )
                }

                // Set up RecyclerView
                val recyclerView = findViewById<RecyclerView>(R.id.absencesRecyclerView)
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = AbsencesAdapter(absencesList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Fehler beim Laden der Abwesenheiten", Toast.LENGTH_SHORT).show()
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