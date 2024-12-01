package com.example.stundenmanager


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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

        // Menü-Button-Listener
        setupMenuNavigation()
    }

    private fun setupMenuNavigation() {
        findViewById<ImageButton>(R.id.menu_home).setOnClickListener {
            Toast.makeText(this, "Du bist bereits auf der Startseite", Toast.LENGTH_SHORT).show()
        }
        findViewById<ImageButton>(R.id.menu_workhours).setOnClickListener {
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
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
    }
}