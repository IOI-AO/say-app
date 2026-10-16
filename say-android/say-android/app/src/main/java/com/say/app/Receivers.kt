package com.say.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val type = i.getStringExtra("type") ?: return
        val title = i.getStringExtra("title") ?: "سَعْي"
        val text = i.getStringExtra("text") ?: ""
        val id = i.getIntExtra("id", 0)
        Scheduler.ensureChannels(c)
        when (type) {
            "prayer" -> {
                val at = i.getLongExtra("at", System.currentTimeMillis())
                try {
                    c.startForegroundService(Intent(c, PrayerService::class.java).putExtra("name", title).putExtra("adhanAt", at))
                } catch (e: Exception) {
                    Scheduler.notify(c, "say_main", id, "حان وقت صلاة $title", "")
                }
            }
            "wake" -> Scheduler.notify(c, "say_wake", id, title, text)
            "fajr" -> Scheduler.notify(c, "say_quiet", id, title, text)
            else -> Scheduler.notify(c, "say_main", id, title, text)
        }
        Scheduler.scheduleAll(c)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        Scheduler.scheduleAll(c)
    }
}
