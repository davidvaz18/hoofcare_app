package com.example.hoof_care_02

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            // TODO: Aqui você precisaria buscar TODOS os lembretes do seu banco de dados
            // (seja um banco local ou do backend) e chamar o AlarmScheduler.scheduleRepeatingAlarm
            // para cada um deles, para reagendá-los.
        }
    }
}