package com.example.mytodolist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Task Reminder"
        val content = intent.getStringExtra("content") ?: "It's time"
        val id = intent.getLongExtra("id", 0L).toInt()
        val soundIdx = intent.getIntExtra("soundIdx", 0)

        val granted = if (android.os.Build.VERSION.SDK_INT >= 33) ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED else true
        if (granted) {
            val builder = NotificationCompat.Builder(context, "todo_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
            try {
                NotificationManagerCompat.from(context).notify(id, builder.build())
            } catch (_: SecurityException) { }
        }

        val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        val tone = when (soundIdx % 10) {
            0 -> ToneGenerator.TONE_PROP_BEEP
            1 -> ToneGenerator.TONE_PROP_BEEP2
            2 -> ToneGenerator.TONE_PROP_ACK
            3 -> ToneGenerator.TONE_PROP_NACK
            4 -> ToneGenerator.TONE_PROP_PROMPT
            5 -> ToneGenerator.TONE_CDMA_PIP
            6 -> ToneGenerator.TONE_SUP_RINGTONE
            7 -> ToneGenerator.TONE_SUP_PIP
            8 -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
            else -> ToneGenerator.TONE_SUP_DIAL
        }
        tg.startTone(tone, 600)
    }
}
