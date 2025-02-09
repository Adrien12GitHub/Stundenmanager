package com.example.stundenmanager

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stundenmanager.absences.AbsencesActivity
import com.example.stundenmanager.workhours.WorkHoursActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

@Suppress("DEPRECATION")
class MessagesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        // highlight menuIcon
        MainActivity.highlightActiveMenu(this, R.id.menu_messages)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        fetchUserIds { users ->
            Log.d("MessagesActivity", "Fetched user IDs: $users")

            fetchUserShift(userId) { initialShift ->
                Log.d("MessagesActivity", "Fetched initial shift: $initialShift")
                val shifts = assignShiftsForUser(userId, initialShift)
                Log.d("MessagesActivity", "Assigned shifts: $shifts")
                val recyclerView: RecyclerView = findViewById(R.id.rvShifts)
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = ShiftAdapter(shifts)
            }
        }

        // Menü-Button-Listener
        setupMenuNavigation()
    }

    private fun fetchUserIds(callback: (List<String>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val userIds = result.map { it.id }
                Log.d("MessagesActivity", "User IDs fetched: $userIds")
                callback(userIds)
            }
            .addOnFailureListener { exception ->
                Log.e("MessagesActivity", "Error fetching user IDs", exception)
            }
    }

    private fun fetchUserShift(userId: String, callback: (String) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val shift = document.getString("shift") ?: "morning" // Default to "morning" if not found
                    callback(shift)
                    // Schedule notification as soon as the shift has been loaded
                    val shiftStartHour = if (shift == "morning") 6 else 14
                    scheduleShiftReminder(this, shiftStartHour, if (shift == "morning") "Early shift" else "Late shift")
                } else {
                    callback("morning") // Default shift in case of error
                }
            }
            .addOnFailureListener {
                callback("morning") // Default shift in case of error
            }
    }

    private fun assignShiftsForUser(userId: String, initialShift: String): List<Shift> {
        val shifts = mutableListOf<Shift>()
        val calendar = Calendar.getInstance()

        // Shift times
        val earlyShiftStartHour = 6
        val earlyShiftEndHour = 14
        val lateShiftStartHour = 14
        val lateShiftEndHour = 22

        // Early or late shift this week
        val isEarlyShiftThisWeek = (initialShift == "morning")

        // Shifts for Monday to Friday
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek != Calendar.MONDAY) {
            // Set to Monday of the current week
            calendar.add(Calendar.DAY_OF_MONTH, Calendar.MONDAY - dayOfWeek)
        }

        while (shifts.size < 5) {
            // Skip weekends
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                continue
            }

            val startTime = calendar.time.apply {
                hours = if (isEarlyShiftThisWeek) earlyShiftStartHour else lateShiftStartHour
                minutes = 0
            }
            val endTime = calendar.time.apply {
                hours = if (isEarlyShiftThisWeek) earlyShiftEndHour else lateShiftEndHour
                minutes = 0
            }

            shifts.add(Shift(userId, startTime, endTime))

            Log.d("assignShiftsForUser", "Assigned shift: ${shifts.last()}")

            // Next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return shifts
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleShiftReminder(context: Context, shiftStartHour: Int, shiftType: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, shiftStartHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // If the time has already passed, set a reminder for the next day
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, ShiftReminderReceiver::class.java).apply {
            putExtra("SHIFT_TYPE", shiftType)
            putExtra("NOTIFICATION_ID", shiftStartHour) // Unique ID
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            shiftStartHour, // Unique ID per alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
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
            Toast.makeText(this, "Du bist bereits auf den Nachrichtenseite", Toast.LENGTH_SHORT).show()
        }
        findViewById<ImageButton>(R.id.menu_reports).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        findViewById<ImageButton>(R.id.menu_statistics).setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
    }
}