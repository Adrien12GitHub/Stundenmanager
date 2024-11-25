package com.example.stundenmanager


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.stundenmanager.workhours.WorkHoursActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val email = user?.email
        val username = email?.substringBefore('@') ?: "Benutzer"

        val welcomeTextView: TextView = findViewById(R.id.welcomeTextView)
        welcomeTextView.text = "Willkommen, $username!"

        findViewById<Button>(R.id.workHoursButton).setOnClickListener {
            startActivity(Intent(this, WorkHoursActivity::class.java))
        }
        findViewById<Button>(R.id.absencesButton).setOnClickListener {
            startActivity(Intent(this, AbsencesActivity::class.java))
        }
        findViewById<Button>(R.id.messagesButton).setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }
        findViewById<Button>(R.id.reportsButton).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        findViewById<Button>(R.id.statisticsButton).setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

    }
}