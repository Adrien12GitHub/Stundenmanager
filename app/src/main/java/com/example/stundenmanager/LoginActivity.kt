package com.example.stundenmanager

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // User already logged in?
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        // Login-Button-Listener
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("LoginActivity", "signIn successful")
                        // Login successful, redirect to HomeActivity
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.d("LoginActivity", "signIn failed")
                        // If login fails, display a message to the user.
                        Toast.makeText(baseContext, "Login failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // ‘Forgot password’ TextView
        findViewById<TextView>(R.id.forgotPasswordTextView).setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
        Log.d("LoginActivity", "showForgotPasswordDialog")
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.forgot_password))

        val input = EditText(this)
        input.hint = getString(R.string.enter_email)
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.reset)) { _, _ ->
            val email = input.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_email), Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            resetPassword(email)
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.show()
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Log.d("LoginActivity", "resetPassword: Sending a reset e-mail successful")
                Toast.makeText(this, getString(R.string.reset_password), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "resetPassword: Error sending reset email", e)
                Toast.makeText(this, getString(R.string.reset_password_failed), Toast.LENGTH_SHORT).show()
            }
    }
}