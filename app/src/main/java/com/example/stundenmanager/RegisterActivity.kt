package com.example.stundenmanager

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        findViewById<Button>(R.id.registerButton).setOnClickListener {
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            registerUser(email, password)
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    assignInitialShift { shift ->
                        val user = hashMapOf(
                            "email" to email,
                            "shift" to shift
                        )
                        firestore.collection("users").document(userId).set(user)
                            .addOnSuccessListener {
                                finish() // Registration successful
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(baseContext, "Error saving user: ${e.message}",
                                    Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // If registration fails, display a message to the user.
                    Toast.makeText(baseContext, "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun assignInitialShift(callback: (String) -> Unit) {
        val usersCollection = firestore.collection("users")
        usersCollection.get().addOnSuccessListener { result ->
            val userCount = result.size()
            val shift = if (userCount % 2 == 0) "morning" else "evening"
            callback(shift)
        }.addOnFailureListener {
            callback("morning") // Default shift in case of error
        }
    }
}
