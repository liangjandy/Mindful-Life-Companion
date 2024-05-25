package com.example.MindfulLifeCompanion.Activity
import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.MindfulLifeCompanion.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationMessage = intent.getStringExtra("notificationMessage") ?: ""
        createNotification(context, notificationMessage)
    }

    private fun createNotification(context: Context, notificationMessage: String) {
        val channelId = "medication_reminder_channel"
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Reminder")
            .setContentText(notificationMessage.ifEmpty { "Take your medications" })
            .setSmallIcon(R.drawable.notification_icon)
            .setAutoCancel(true)

        val appIntent = Intent(context, MedicationReminderActivity::class.java)
        appIntent.putExtra("notificationMessage", notificationMessage) // Add this line
        val appPendingIntent =
            PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_IMMUTABLE)
        notificationBuilder.setContentIntent(appPendingIntent)

        val notificationId = 1
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}