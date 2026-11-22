package com.example.test.ui.home.Screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.test.platform.notification.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private enum class NotificationAction {
    SEND_NOW,
    SCHEDULE
}

private fun defaultAlarmTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
    Calendar.getInstance().apply { add(Calendar.MINUTE, 1) }.time
)

@Composable
fun NotificationAlarmDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var timeInput by rememberSaveable { mutableStateOf(defaultAlarmTime()) }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingAction by remember { mutableStateOf<NotificationAction?>(null) }
    var showExactAlarmSettings by remember { mutableStateOf(false) }

    val scheduleAlarm: () -> Unit = {
        val parts = Regex("^(\\d{2}):(\\d{2})$").matchEntire(timeInput)
        val hour = parts?.groupValues?.get(1)?.toIntOrNull()
        val minute = parts?.groupValues?.get(2)?.toIntOrNull()
        if (hour == null || minute == null || hour !in 0..23 || minute !in 0..59) {
            feedback = "Nhập giờ hợp lệ theo dạng HH:mm, ví dụ 14:30."
            showExactAlarmSettings = false
        } else {
            when (val result = NotificationHelper.scheduleAtNextOccurrence(context, hour, minute)) {
                is NotificationHelper.ScheduleResult.Scheduled -> {
                    feedback = "Đã đặt alarm: ${NotificationHelper.formatTriggerTime(result.triggerAtMillis)}"
                    showExactAlarmSettings = false
                }
                NotificationHelper.ScheduleResult.ExactAlarmPermissionRequired -> {
                    feedback = "Cần cấp quyền Alarms & reminders trong cài đặt Android."
                    showExactAlarmSettings = true
                }
            }
        }
    }

    val runAction: (NotificationAction) -> Unit = { action ->
        when (action) {
            NotificationAction.SEND_NOW -> {
                NotificationHelper.showImmediate(context)
                feedback = "Đã gửi notification thử."
            }
            NotificationAction.SCHEDULE -> scheduleAlarm()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val action = pendingAction
        pendingAction = null
        if (granted && action != null) runAction(action)
        else feedback = "Cần cấp quyền notification để hiển thị nhắc việc."
    }

    val exactAlarmSettingsLauncher = rememberLauncherForActivityResult(
        contract = StartActivityForResult()
    ) {
        if (NotificationHelper.canScheduleExactAlarms(context)) scheduleAlarm()
        else feedback = "Quyền exact alarm chưa được bật."
    }

    fun requestAction(action: NotificationAction) {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            pendingAction = action
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            runAction(action)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nhắc việc và alarm") },
        text = {
            Column(
                modifier = Modifier.widthIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Gửi notification thử hoặc đặt notification theo giờ địa phương.")
                OutlinedTextField(
                    value = timeInput,
                    onValueChange = { input ->
                        timeInput = input.filter { it.isDigit() || it == ':' }.take(5)
                        feedback = null
                    },
                    label = { Text("Thời gian (HH:mm)") },
                    placeholder = { Text("14:30") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { requestAction(NotificationAction.SEND_NOW) }) {
                        Text("Gửi thử")
                    }
                    TextButton(onClick = { requestAction(NotificationAction.SCHEDULE) }) {
                        Text("Đặt alarm")
                    }
                }
                if (showExactAlarmSettings) {
                    TextButton(onClick = {
                        exactAlarmSettingsLauncher.launch(
                            NotificationHelper.exactAlarmSettingsIntent(context)
                        )
                    }) {
                        Text("Mở cài đặt quyền alarm")
                    }
                }
                TextButton(onClick = {
                    NotificationHelper.cancelAlarm(context)
                    showExactAlarmSettings = false
                    feedback = "Đã hủy alarm đang chờ."
                }) {
                    Text("Hủy alarm")
                }
                feedback?.let { Text(it) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}
