package com.example.hoof_care_02

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.hoof_care_02.data.repository.ReminderRepository
import com.example.hoof_care_02.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Reagendando alarmes após reinicialização...")
            
            val pendingResult = goAsync()
            
            scope.launch {
                try {
                    val reminders = ReminderRepository.getAllUserReminders()
                    for (reminder in reminders) {
                        try {
                            val h = reminder.time.substring(0, 2).toInt()
                            val m = reminder.time.substring(3, 5).toInt()
                            
                            AlarmScheduler.scheduleRepeatingAlarm(
                                context, h, m, reminder.id,
                                reminder.getDisplayTitle(),
                                reminder.getDisplayDescription()
                            )
                        } catch (e: Exception) {
                            Log.e("BootReceiver", "Erro ao reagendar lembrete ${reminder.id}", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Falha ao buscar lembretes no Firestore", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
