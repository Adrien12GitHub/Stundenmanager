package com.example.stundenmanager

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

class MessagesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        fetchUserIds { users ->
            Log.d("MessagesActivity", "Fetched user IDs: $users")
            val shifts = assignShiftsForUser(userId, users)
            Log.d("MessagesActivity", "Assigned shifts: $shifts")
            val recyclerView: RecyclerView = findViewById(R.id.rvShifts)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = ShiftAdapter(shifts)
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

    private fun assignShiftsForUser(userId: String, users: List<String>): List<Shift> {
        val shifts = mutableListOf<Shift>()
        val calendar = Calendar.getInstance()

        // Shift times
        val earlyShiftStartHour = 6
        val earlyShiftEndHour = 14
        val lateShiftStartHour = 14
        val lateShiftEndHour = 22

        // Current week
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)

        // Early or late shift this week
        val userIndex = users.indexOf(userId)
        val isEarlyShiftThisWeek = (currentWeek % 2 == 0) == (userIndex % 2 == 0)

        // Shifts for Monday to Friday
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek != Calendar.MONDAY) {
            // Setze auf Montag der aktuellen Woche
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