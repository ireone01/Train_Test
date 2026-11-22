package com.example.test.platform.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.test.MainActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object NotificationHelper {
    private const val CHANNEL_ID = "inventory_reminders"
    private const val IMMEDIATE_NOTIFICATION_ID = 4101
    private const val ALARM_NOTIFICATION_ID = 4102
    private const val ALARM_REQUEST_CODE = 4201

    sealed interface ScheduleResult {
        data class Scheduled(val triggerAtMillis: Long) : ScheduleResult
        data object ExactAlarmPermissionRequired : ScheduleResult
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Nhắc việc VNPT Inventory",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Thông báo nhắc việc và alarm thử nghiệm"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun showImmediate(context: Context) {
        show(
            context = context,
            notificationId = IMMEDIATE_NOTIFICATION_ID,
            title = "VNPT Inventory",
            message = "Thông báo thử đã được gửi."
        )
    }

    fun scheduleAtNextOccurrence(
        context: Context,
        hourOfDay: Int,
        minute: Int
    ): ScheduleResult {
        require(hourOfDay in 0..23)
        require(minute in 0..59)

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return ScheduleResult.ExactAlarmPermissionRequired
        }

        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        val alarmIntent = Intent(context, NotificationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            trigger.timeInMillis,
            pendingIntent
        )
        return ScheduleResult.Scheduled(trigger.timeInMillis)
    }

    fun cancelAlarm(context: Context) {
        val alarmIntent = Intent(context, NotificationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun exactAlarmSettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
        }

    fun canScheduleExactAlarms(context: Context): Boolean {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    }

    fun formatTriggerTime(triggerAtMillis: Long): String =
        SimpleDateFormat("EEE, HH:mm", Locale.getDefault()).format(Date(triggerAtMillis))

    internal fun showAlarmNotification(context: Context) {
        show(
            context = context,
            notificationId = ALARM_NOTIFICATION_ID,
            title = "Đến giờ nhắc việc",
            message = "Bạn có một nhắc việc đã được lên lịch."
        )
    }

    private fun show(context: Context, notificationId: Int, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        createChannel(context)
        val openAppIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(openAppIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
