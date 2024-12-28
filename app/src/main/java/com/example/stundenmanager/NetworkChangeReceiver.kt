package com.example.stundenmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NetworkChangeReceiver", "onReceive")
        if (NetworkUtils.isConnected(context)) {
            Log.d("NetworkChangeReceiver", "Network is connected. Syncing offline data.")
            OfflineDataManager.syncOfflineData(context) // Synchronise when online
        }
    }
}