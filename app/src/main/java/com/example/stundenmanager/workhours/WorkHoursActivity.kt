package com.example.stundenmanager.workhours

import com.example.stundenmanager.R
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.stundenmanager.absences.AbsencesActivity
import com.example.stundenmanager.HomeActivity
import com.example.stundenmanager.MessagesActivity
import com.example.stundenmanager.NetworkUtils
import com.example.stundenmanager.OfflineDataStore
import com.example.stundenmanager.ReportsActivity
import com.example.stundenmanager.StatisticsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Calendar

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
                stopTracking(toggleTrackButton) // Stop
            } else {
                startTracking(toggleTrackButton) // Start
            }
        }

        addButton.setOnClickListener {
            openManualEntryDialog()
        }

        restoreTrackingState() // Restore tracking state if app is restarted
        fetchWorkHours() // Load the data at startup

        // Menü-Button-Listener
        setupMenuNavigation()
    }

    private fun startTracking(button: Button) {
        Log.d("WorkHoursActivity.kt", "startTracking")
        startTime = Timestamp.now()
        isTracking = true
        persistTrackingState(true, startTime)
        startTrackingService()

        button.setCompoundDrawablesWithIntrinsicBounds(
            android.R.drawable.ic_media_pause, 0, 0, 0
        )

        Toast.makeText(this, getString(R.string.tracking_started), Toast.LENGTH_SHORT).show()
    }

    private fun stopTracking(button: Button) {
        Log.d("WorkHoursActivity.kt", "stopTracking")
        val endTime = Timestamp.now()
        isTracking = false
        persistTrackingState(false, null)  // End tracking and delete start time
        stopTrackingService()

        button.setCompoundDrawablesWithIntrinsicBounds(
            android.R.drawable.ic_media_play, 0, 0, 0
        )

        if (startTime != null) {
            Log.d("WorkHoursActivity.kt", "stopTracking: startTime != null")
            val workedMillis = endTime.toDate().time - startTime!!.toDate().time
            val workedHours = workedMillis / (1000 * 60 * 60.0) // Calculate hours

            checkForOverlappingWorkHours(startTime!!, endTime) { overlap ->
                if (!overlap) {
                    saveWorkHours(
                        startTime = startTime!!,
                        endTime = endTime,
                        breakDuration = 0, // No pause with automatic tracking
                        comment = getString(R.string.auto_start),
                        hoursWorked = workedHours
                    )
                    startTime = null
                    Toast.makeText(this, getString(R.string.tracking_stopped), Toast.LENGTH_SHORT).show()
                    fetchWorkHours()
                } else {
                    Toast.makeText(
                        this,
                        "Die eingegebenen Zeiten überschneiden sich mit einer bestehenden Arbeitszeit",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Log.e("WorkHoursActivity.kt", "stopTracking: startTime is null")
        }
    }

    private fun restoreTrackingState() {
        Log.d("WorkHoursActivity.kt", "restoreTrackingState")
        val sharedPreferences = getSharedPreferences("tracking_prefs", MODE_PRIVATE)
        val isTrackingPersisted = sharedPreferences.getBoolean("is_tracking", false)
        val startTimeSeconds = sharedPreferences.getLong("start_time", -1)

        if (isTrackingPersisted && startTimeSeconds != -1L) {
            Log.d("WorkHoursActivity.kt", "restoreTrackingState: isTracking = true")
            startTime = Timestamp(startTimeSeconds, 0)
            startTrackingService()
            isTracking = true
            findViewById<Button>(R.id.toggleTrackButton).setCompoundDrawablesWithIntrinsicBounds(
                android.R.drawable.ic_media_pause, 0, 0, 0
            )
        } else {
            Log.d("WorkHoursActivity.kt", "restoreTrackingState: isTracking = false")
            isTracking = false

            // Set the button to the play icon (tracking is not running)
            findViewById<Button>(R.id.toggleTrackButton).setCompoundDrawablesWithIntrinsicBounds(
                android.R.drawable.ic_media_play, 0, 0, 0
            )
        }

    }

    private fun persistTrackingState(isTracking: Boolean, startTime: Timestamp?) {
        Log.d("WorkHoursActivity.kt", "persistTrackingState")
        val sharedPreferences = getSharedPreferences("tracking_prefs", MODE_PRIVATE)
        //sharedPreferences.edit().putBoolean("is_tracking", isTracking).apply()
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_tracking", isTracking) // Saving the tracking status
        startTime?.let { editor.putLong("start_time", it.seconds) } // Save the start time (if available)
        editor.apply()
    }

    private fun startTrackingService() {
        Log.d("WorkHoursActivity.kt", "startTrackingService")
        val intent = Intent(this, TrackingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent) // For Android 8.0 and higher
        } else {
            startService(intent) // For older versions
        }
    }

    private fun stopTrackingService() {
        Log.d("WorkHoursActivity.kt", "stopTrackingService")
        val intent = Intent(this, TrackingService::class.java)
        stopService(intent)
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

            // Check for overlapping work hours
            checkForOverlappingWorkHours(Timestamp(Date(startDateTime.time)), Timestamp(Date(endDateTime.time))) { overlap ->
                if (!overlap) {
                    // Storage of data in Firestore
                    saveWorkHours(
                        startTime = Timestamp(Date(startDateTime.time)),
                        endTime = Timestamp(Date(endDateTime.time)),
                        breakDuration = breakDuration,
                        comment = comment,
                        hoursWorked = workedHours
                    )
                } else {
                    Log.d("WorkHoursActivity.kt", "saveManualWorkHours: Overlap")
                    Toast.makeText(
                        this,
                        "Die eingegebenen Zeiten überschneiden sich mit einer bestehenden Arbeitszeit",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            Log.e("WorkHoursActivity.kt", "saveManualWorkHours: Exception")
            Toast.makeText(this, getString(R.string.calc_error) + {e.message}, Toast.LENGTH_SHORT).show()
        }
        fetchWorkHours()
    }

    private fun checkForOverlappingWorkHours(startTime: Timestamp, endTime: Timestamp, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("workHours")
            .get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val existingStartTime = document.getTimestamp("startTime") ?: continue
                    val existingEndTime = document.getTimestamp("endTime") ?: continue

                    // Check if the new work hours overlap with existing work hours
                    if (startTime.toDate().before(existingEndTime.toDate()) && endTime.toDate().after(existingStartTime.toDate())) {
                        callback(true) // Overlap found
                        return@addOnSuccessListener
                    }
                }
                callback(false) // No overlap
            }
            .addOnFailureListener {
                Toast.makeText(this, "Fehler beim Überprüfen der Arbeitszeiten", Toast.LENGTH_SHORT).show()
                callback(true) // Treat as overlap to prevent saving
            }
    }

    private fun saveWorkHours(
        startTime: Timestamp,
        endTime: Timestamp,
        breakDuration: Int,
        comment: String,
        hoursWorked: Double
    ) {
        if (isWeekend(startTime)) {
            Log.d("WorkHoursActivity.kt", "saveWorkHours: Booking not permitted at weekends")
            Toast.makeText(this, getString(R.string.booking_weekend), Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("WorkHoursActivity.kt", "saveWorkHours")
        val userId = auth.currentUser?.uid ?: return
        Log.d("WorkHoursActivity.kt", "User ID: $userId")

        val workHoursData = WorkHour(
            startTime = startTime,
            endTime = endTime,
            breakDuration = breakDuration,
            hoursWorked = hoursWorked,
            comment = comment
        )

        if (NetworkUtils.isConnected(this)) {
            // Online: Save in Firebase
            db.collection("users").document(userId).collection("workHours")
                .add(workHoursData.toHashMap())
                .addOnSuccessListener {
                    Log.d("WorkHoursActivity.kt", "saveWorkHours: addOnSuccessListener")
                    Toast.makeText(this, getString(R.string.time_saved), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.e("WorkHoursActivity.kt", "saveWorkHours: addOnFailureListener")
                    Toast.makeText(this, getString(R.string.saving_error), Toast.LENGTH_SHORT).show()
                    OfflineDataStore.addWorkHour(workHoursData) // Save offline
                }
        } else {
            // Offline: Save directly to RAM
            Log.d("WorkHoursActivity.kt", "Offline mode: Saving to OfflineDataStore")
            OfflineDataStore.addWorkHour(workHoursData)
            Toast.makeText(this, getString(R.string.saved_offline), Toast.LENGTH_SHORT).show()
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

    private fun setupMenuNavigation() {
        findViewById<ImageButton>(R.id.menu_home).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        findViewById<ImageButton>(R.id.menu_workhours).setOnClickListener {
            Toast.makeText(this, "Du bist bereits auf der Arbeitszeiten-Seite", Toast.LENGTH_SHORT).show()
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
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
    }

    fun syncOfflineData() {
        Log.d("WorkHoursActivity.kt", "syncOfflineData")
        if (!NetworkUtils.isConnected(this)) return // If offline, do nothing

        val userId = auth.currentUser?.uid ?: return
        val unsyncedWorkHours = OfflineDataStore.getUnsyncedWorkHours()
        if (unsyncedWorkHours.isEmpty()) {
            Log.d("WorkHoursActivity.kt", "No offline data to sync")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        unsyncedWorkHours.forEach { workHour ->
            firestore.collection("users").document(userId).collection("workHours")
                .add(workHour.toHashMap())
                .addOnSuccessListener {
                    Log.d("WorkHoursActivity.kt", "syncOfflineData: Synced successfully")
                    OfflineDataStore.clearSyncedWorkHours() // Delete successfully synchronised data
                    Toast.makeText(this, getString(R.string.synced_data), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.e("WorkHoursActivity.kt", "syncOfflineData: Sync failed")
                }
        }
    }

    private fun isWeekend(timestamp: Timestamp): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = timestamp.toDate()

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
    }
}
