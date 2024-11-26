package com.example.stundenmanager.workhours

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.stundenmanager.R

class TrackingService : Service() {

    companion object {
        const val CHANNEL_ID = "TrackingServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        Log.d("TrackingService.kt", "onCreate")
        super.onCreate()
        createNotificationChannel()
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TrackingService.kt", "onStartCommand")
        val content = getString(R.string.tracking_notification_content)
        val notification = createNotification(content)
        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.d("TrackingService.kt", "onDestroy")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null // No binder communication is used

    private fun createNotification(contentText: String): Notification {
        Log.d("TrackingService.kt", "createNotification")
        val intent = Intent(this, WorkHoursActivity::class.java) // Back to the main activity
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Flags for newer Android versions
        )

        val title = getString(R.string.app_name)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_media_play) // Icon for the notification
            .setPriority(NotificationCompat.PRIORITY_LOW) // Low priority for continuous notifications
            .setOngoing(true) // Not removable by the user
            .setContentIntent(pendingIntent) // Click on the notification to open the app
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        Log.d("TrackingService.kt", "createNotificationChannel")
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Tracking Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

}