package com.example.stundenmanager.absences

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.stundenmanager.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object AbsencesSyncManager {
    fun syncOfflineAbsences(context: Context) {
        Log.d("AbsencesSyncManager", "syncOfflineAbsences called")

        // Check whether the device is connected to the Internet
        if (!NetworkUtils.isConnected(context)) {
            Log.d("AbsencesSyncManager", "Device is offline. Sync aborted.")
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("AbsencesSyncManager", "User not logged in. Sync aborted.")
            return
        }

        val unsyncedAbsences = OfflineAbsencesStore.getUnsyncedAbsences()
        if (unsyncedAbsences.isEmpty()) {
            Log.d("AbsencesSyncManager", "No offline data to sync")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        unsyncedAbsences.forEach { absence ->
            val absenceData = hashMapOf(
                "reason" to absence.reason,
                "dateFrom" to absence.dateFrom,
                "dateTo" to absence.dateTo
            )
            firestore.collection("users").document(userId).collection("absences")
                .add(absenceData)
                .addOnSuccessListener {
                    Log.d("AbsencesSyncManager", "Absence synced successfully")
                    OfflineAbsencesStore.clearSyncedAbsences()
                    Toast.makeText(context, "Abwesenheiten erfolgreich synchronisiert", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("AbsencesSyncManager", "Error syncing absence: ${e.message}")
                }
        }
    }
}
