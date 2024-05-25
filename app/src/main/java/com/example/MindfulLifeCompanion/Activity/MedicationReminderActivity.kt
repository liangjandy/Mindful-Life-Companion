package com.example.MindfulLifeCompanion.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.MindfulLifeCompanion.R
import java.util.*

class MedicationReminderActivity : AppCompatActivity() {

    private lateinit var alarmManager: AlarmManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var alarmIntent: PendingIntent

    private lateinit var selectTimeTextView: TextView
    private lateinit var setAlarmButton: Button
    private lateinit var cancelAlarmButton: Button
    private lateinit var backButton: Button
    private lateinit var cancelNotificationMessageButton: Button
    private lateinit var setNotificationMessageButton: Button
    private var hourOfDay: Int = 0
    private var minute: Int = 0
    private var notificationMessage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medication_reminder)

        selectTimeTextView = findViewById(R.id.selectTime)
        setAlarmButton = findViewById(R.id.setAlarm)
        cancelAlarmButton = findViewById(R.id.cancelAlarm)
        backButton = findViewById(R.id.BackButton)
        setNotificationMessageButton = findViewById(R.id.setNotificationMessage)
        cancelNotificationMessageButton = findViewById(R.id.deleteNotificationMessage)

        setNotificationMessageButton.setOnClickListener {
            showSetNotificationMessageDialog()
        }

        cancelNotificationMessageButton.setOnClickListener {
            notificationMessage = ""
        }

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        setAlarmButton.setOnClickListener {
            showTimePickerDialog()
        }

        cancelAlarmButton.setOnClickListener {
            cancelAlarm()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun showTimePickerDialog() {
        val currentTime = getCurrentTime()
        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHourOfDay, selectedMinute ->
                hourOfDay = selectedHourOfDay
                minute = selectedMinute
                setAlarm(hourOfDay, minute)
            },
            currentTime.first, // Initial hour
            currentTime.second, // Initial minute
            false // 24-hour format
        )
        timePickerDialog.show()
    }

    private fun getCurrentTime(): Pair<Int, Int> {
        // Implement your logic to get the current hour and minute
        // Here, we'll assume the current time is 9:00 AM
        return Pair(9, 0)
    }

    private fun setAlarm(hour: Int, minute: Int) {
        val currentTime = getCurrentTime()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

        }

        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("notificationMessage", notificationMessage)

        val requestCode = hour * 100 + minute // Unique request code based on hour and minute
        alarmIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_MUTABLE)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)

        // Update the UI to display the selected time
        selectTimeTextView.text = String.format("%02d:%02d", hour, minute)
    }

    private fun cancelAlarm() {
        alarmManager.cancel(alarmIntent)
        selectTimeTextView.text = "Select \nTime"
    }

    private fun showSetNotificationMessageDialog() {
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.dialog_set_notification_message, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
            .setTitle("Set Reminder")
            .setView(dialogView)
            .setPositiveButton("Set") { dialog: DialogInterface?, _: Int ->
                val editTextNotificationMessage =
                    dialogView.findViewById<EditText>(R.id.editTextNotificationMessage)
                notificationMessage = editTextNotificationMessage.text.toString().trim()
                // You can save the notification message in SharedPreferences or a database if needed
            }
            .setNegativeButton("Cancel", null)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}