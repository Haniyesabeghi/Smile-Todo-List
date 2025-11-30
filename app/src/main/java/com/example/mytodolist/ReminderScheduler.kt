package com.example.mytodolist

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object ReminderScheduler {
    fun scheduleExact(context: Context, item: TodoItem) {
        val day = item.dueEpochDay ?: return
        val hour = item.reminderHour ?: return
        val minute = item.reminderMinute ?: 0
        val dt = LocalDate.ofEpochDay(day).atTime(hour, minute)
        val millis = dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val am = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", item.title)
            putExtra("content", item.time)
            putExtra("id", item.id)
            putExtra("soundIdx", item.soundIndex ?: 0)
        }
        val pi = PendingIntent.getBroadcast(context, item.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
    }
}
