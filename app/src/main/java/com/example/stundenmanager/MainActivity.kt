package com.example.stundenmanager

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
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
        registerReceiver(networkChangeReceiver, intentFilter)

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
}

