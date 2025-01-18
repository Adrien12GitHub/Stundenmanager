package com.example.stundenmanager.absences

import com.google.firebase.Timestamp

object OfflineAbsencesStore {
    private val unsyncedAbsences = mutableListOf<Absence>()

    // Method for adding a new absence
    fun addAbsence(absence: Absence) {
        unsyncedAbsences.add(absence)
    }

    // Method for retrieving all unsynchronised absences
    fun getUnsyncedAbsences(): List<Absence> {
        return unsyncedAbsences.toList() // Returns a copy of the list
    }

    // Method for deleting synchronised absences
    fun clearSyncedAbsences() {
        unsyncedAbsences.clear()
    }
}