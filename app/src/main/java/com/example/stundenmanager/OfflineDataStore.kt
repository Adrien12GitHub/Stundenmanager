package com.example.stundenmanager

import com.example.stundenmanager.workhours.WorkHour

object OfflineDataStore {
    private val unsyncedWorkHours = mutableListOf<WorkHour>()

    // Method for adding a new working time
    fun addWorkHour(workHour: WorkHour) {
        unsyncedWorkHours.add(workHour)
    }

    // Method to retrieve all non-synchronised working times
    fun getUnsyncedWorkHours(): List<WorkHour> {
        return unsyncedWorkHours.toList() // Return of a copy of the list
    }

    // Method for deleting synchronised data
    fun clearSyncedWorkHours() {
        unsyncedWorkHours.clear()
    }
}