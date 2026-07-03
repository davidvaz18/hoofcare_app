package com.example.hoof_care_02.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.hoof_care_02.NotificationReceiver
import java.util.Calendar

object AlarmScheduler {
    fun scheduleRepeatingAlarm(context: Context, hour: Int, minute: Int, reminderId: String, title: String, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Conversão determinística de String para Int para o PendingIntent
        val requestCode = reminderId.hashCode()

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminderId)
            putExtra("REMINDER_TITLE", title)
            putExtra("REMINDER_MESSAGE", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            // Se o horário já passou hoje, agenda para o dia seguinte
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        // Agenda um alarme que se repete todos os dias
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    /**
     * Cancela um alarme previamente agendado para um lembrete.
     * Deve ser chamado ao excluir ou editar (antes de reagendar) um lembrete.
     */
    fun cancelAlarm(context: Context, reminderId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = reminderId.hashCode()

        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}