package com.example.stundenmanager

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var networkChangeReceiver: NetworkChangeReceiver
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Dynamic registration of the NetworkChangeReceiver
        networkChangeReceiver = NetworkChangeReceiver()
        val intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        //registerReceiver(networkChangeReceiver, intentFilter)

        Log.d("MainActivity", "NetworkChangeReceiver dynamically registered")

        auth = FirebaseAuth.getInstance()

        // Check whether user is logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User logged in, forward to HomeActivity
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        val loginButton: Button = findViewById(R.id.loginButton)
        val registerButton: Button = findViewById(R.id.registerButton)

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    companion object {
        fun highlightActiveMenu(activity: Activity, menuItemId: Int) {
            val home = activity.findViewById<ImageButton>(R.id.menu_home)
            val workhours = activity.findViewById<ImageButton>(R.id.menu_workhours)
            val absences = activity.findViewById<ImageButton>(R.id.menu_absences)
            val messages = activity.findViewById<ImageButton>(R.id.menu_messages)
            val reports = activity.findViewById<ImageButton>(R.id.menu_reports)
            val statistics = activity.findViewById<ImageButton>(R.id.menu_statistics)

            // Set default colour for all icons
            val defaultColor = ContextCompat.getColor(activity, R.color.default_icon_color)
            val activeColor = ContextCompat.getColor(activity, R.color.active_icon_color)

            home.setColorFilter(defaultColor)
            workhours.setColorFilter(defaultColor)
            absences.setColorFilter(defaultColor)
            messages.setColorFilter(defaultColor)
            reports.setColorFilter(defaultColor)
            statistics.setColorFilter(defaultColor)

            // Highlight active menu item
            activity.findViewById<ImageButton>(menuItemId).setColorFilter(activeColor)
        }
    }
}

