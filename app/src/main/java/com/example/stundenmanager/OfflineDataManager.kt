package com.example.stundenmanager

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

object OfflineDataManager {
    fun syncOfflineData(context: Context) {
        Log.d("OfflineDataManager", "syncOfflineData called")

        // Check whether the device is connected
        if (!NetworkUtils.isConnected(context)) {
            Log.d("OfflineDataManager", "Device is offline. Sync aborted.")
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("OfflineDataManager", "User not logged in. Sync aborted.")
            return
        }

        val unsyncedWorkHours = OfflineDataStore.getUnsyncedWorkHours()
        if (unsyncedWorkHours.isEmpty()) {
            Log.d("OfflineDataManager", "No offline data to sync")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        unsyncedWorkHours.forEach { workHour ->
            firestore.collection("users").document(userId).collection("workHours")
                .add(workHour.toHashMap())
                .addOnSuccessListener {
                    Log.d("OfflineDataManager", "Work hour synced successfully")
                    OfflineDataStore.clearSyncedWorkHours()
                    Toast.makeText(context, "Data synced successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("OfflineDataManager", "Error syncing work hour: ${e.message}")
                }
        }
    }
}