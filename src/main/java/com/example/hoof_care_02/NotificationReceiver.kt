package com.example.hoof_care_02

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("REMINDER_TITLE") ?: "Lembrete do seu Pet"
        val message = intent.getStringExtra("REMINDER_MESSAGE") ?: "Está na hora da atividade!"
        val notificationId = intent.getIntExtra("REMINDER_ID", 0)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "PET_REMINDERS_CHANNEL")
            .setSmallIcon(R.drawable.botaomaiscachorro) // Use um ícone seu (ex: pata de cachorro)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}