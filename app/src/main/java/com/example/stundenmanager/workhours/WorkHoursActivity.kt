package com.example.stundenmanager.workhours

import com.example.stundenmanager.R
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.app.Dialog
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

class WorkHoursActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isTracking = false // Flag: Whether tracking is running
    private var startTime: Timestamp? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_work_hours)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val addButton: Button = findViewById(R.id.addButton)

        val toggleTrackButton: Button = findViewById(R.id.toggleTrackButton)
        toggleTrackButton.setOnClickListener {
            if (isTracking) {
                stopTracking( toggleTrackButton) // Stop
            } else {
                startTracking( toggleTrackButton) // Start
            }
        }

        addButton.setOnClickListener {
            openManualEntryDialog()
        }

        fetchWorkHours() // Load the data at startup
    }

    private fun startTracking(button: Button) {
        Log.d("WorkHoursActivity.kt", "startTracking")
        startTime = Timestamp.now()
        isTracking = true

        button.setCompoundDrawablesWithIntrinsicBounds(
            android.R.drawable.ic_media_pause, 0, 0, 0
        )

        Toast.makeText(this, getString(R.string.tracking_started), Toast.LENGTH_SHORT).show()
    }

    private fun stopTracking(button: Button) {
        Log.d("WorkHoursActivity.kt", "stopTracking")
        val endTime = Timestamp.now()
        isTracking = false

        button.setCompoundDrawablesWithIntrinsicBounds(
            android.R.drawable.ic_media_play, 0, 0, 0
        )

        if (startTime != null) {
            val workedMillis = endTime.toDate().time - startTime!!.toDate().time
            val workedHours = workedMillis / (1000 * 60 * 60.0) // Calculate hours

            saveWorkHours(
                startTime = startTime!!,
                endTime = endTime,
                breakDuration = 0, // No pause with automatic tracking
                comment = getString(R.string.auto_start),
                hoursWorked = workedHours
            )

            Toast.makeText(this, getString(R.string.tracking_stopped), Toast.LENGTH_SHORT).show()
            fetchWorkHours()
        }
    }

    private fun openManualEntryDialog() {
        Log.d("WorkHoursActivity.kt", "openManualEntryDialog")
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_work_hours)

        // Set dialogue width to match_parent and height to wrap_content
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, // Width: match_parent
            ViewGroup.LayoutParams.WRAP_CONTENT // Height: wrap_content
        )
        dialog.window?.setGravity(Gravity.CENTER) // This centres the dialogue on the screen.

        val startTimeInput = dialog.findViewById<EditText>(R.id.dialogStartTime)
        val endTimeInput = dialog.findViewById<EditText>(R.id.dialogEndTime)
        val breakInput = dialog.findViewById<EditText>(R.id.dialogBreakDuration)
        val commentInput = dialog.findViewById<EditText>(R.id.dialogComment)
        val saveButton = dialog.findViewById<Button>(R.id.dialogSaveButton)

        saveButton.setOnClickListener {
            val startTime = startTimeInput.text.toString()
            val endTime = endTimeInput.text.toString()
            val breakDuration = breakInput.text.toString().toIntOrNull() ?: 0
            val comment = commentInput.text.toString()

            saveManualWorkHours(startTime, endTime, breakDuration, comment)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveManualWorkHours(
        startTime: String,
        endTime: String,
        breakDuration: Int,
        comment: String
    ) {
        try {
            Log.d("WorkHoursActivity.kt", "saveManualWorkHours: try")
            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Combining date and time
            val startDateTime = dateTimeFormat.parse("$todayDate $startTime")
            val endDateTime = dateTimeFormat.parse("$todayDate $endTime")

            if (startDateTime == null || endDateTime == null) {
                Log.d("WorkHoursActivity.kt", "saveManualWorkHours: if == null")
                Toast.makeText(this, getString(R.string.time_invalid), Toast.LENGTH_SHORT).show()
                return
            }

            // Check: Start time must not be after the end time
            if (startDateTime.after(endDateTime)) {
                Log.d("WorkHoursActivity.kt", "saveManualWorkHours: start after endtime")
                Toast.makeText(this, getString(R.string.start_end_error), Toast.LENGTH_SHORT).show()
                return
            }

            // Calculation of hours worked (milliseconds -> hours)
            val workedMillis = endDateTime.time - startDateTime.time
            val workedHours = (workedMillis / (1000 * 60 * 60)).toDouble() - (breakDuration / 60.0)

            if (workedHours < 0) {
                Log.d("WorkHoursActivity.kt", "saveManualWorkHours: workedHours < 0")
                Toast.makeText(this, getString(R.string.hours_negative), Toast.LENGTH_SHORT).show()
                return
            }

            // Storage of data in Firestore
            saveWorkHours(
                startTime = Timestamp(Date(startDateTime.time)),
                endTime = Timestamp(Date(endDateTime.time)),
                breakDuration = breakDuration,
                comment = comment,
                hoursWorked = workedHours
            )
        } catch (e: Exception) {
            Log.e("WorkHoursActivity.kt", "saveManualWorkHours: Exception")
            Toast.makeText(this, getString(R.string.calc_error) + {e.message}, Toast.LENGTH_SHORT).show()
        }
        fetchWorkHours()
    }

    private fun saveWorkHours(
        startTime: Timestamp,
        endTime: Timestamp,
        breakDuration: Int,
        comment: String,
        hoursWorked: Double
    ) {
        Log.d("WorkHoursActivity.kt", "saveWorkHours")
        val userId = auth.currentUser?.uid ?: return
        val workHoursData = hashMapOf(
            "startTime" to startTime,
            "endTime" to endTime,
            "breakDuration" to breakDuration,
            "hoursWorked" to hoursWorked,
            "comment" to comment
        )

        db.collection("users").document(userId).collection("workHours")
            .add(workHoursData)
            .addOnSuccessListener {
                Log.d("WorkHoursActivity.kt", "saveWorkHours: addOnSuccessListener")
                Toast.makeText(this, getString(R.string.time_saved), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.d("WorkHoursActivity.kt", "saveWorkHours: addOnFailureListener")
                Toast.makeText(this, getString(R.string.saving_error), Toast.LENGTH_SHORT).show()
            }
        fetchWorkHours()
    }

    private fun fetchWorkHours() {
        Log.d("WorkHoursActivity.kt", "fetchWorkHours")
        val userId = auth.currentUser?.uid ?: return
        Log.d("WorkHoursActivity.kt", "fetchWorkHours: userID= $userId")

        db.collection("users").document(userId).collection("workHours")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("WorkHoursActivity.kt", "fetchWorkHours: addOnSuccessListener")
                val workHoursList = mutableListOf<WorkHour>()

                for (document in documents) {
                    val startTime = document.getTimestamp("startTime") ?: continue
                    val endTime = document.getTimestamp("endTime") ?: continue
                    val breakDuration = document.getLong("breakDuration")?.toInt() ?: 0
                    val hoursWorked = document.getDouble("hoursWorked") ?: 0.0
                    val comment = document.getString("comment") ?: ""

                    workHoursList.add(
                        WorkHour(
                            startTime = startTime,
                            endTime = endTime,
                            breakDuration = breakDuration,
                            hoursWorked = hoursWorked,
                            comment = comment
                        )
                    )
                }

                // Initialise RecyclerView
                val recyclerView: RecyclerView = findViewById(R.id.workHoursRecyclerView)
                recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
                recyclerView.adapter = WorkHoursAdapter(workHoursList)
            }
            .addOnFailureListener {
                Log.d("WorkHoursActivity.kt", "fetchWorkHours: addOnFailureListener")
                Toast.makeText(this, getString(R.string.loading_error), Toast.LENGTH_SHORT).show()
            }
    }

}
