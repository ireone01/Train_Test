package com.example.test.platform.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        NotificationHelper.showAlarmNotification(context)
    }
}
