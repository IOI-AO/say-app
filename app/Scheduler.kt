package com.say.app

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import java.time.LocalDate
import java.time.ZoneId

object Scheduler {
    val NAMES = arrayOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")

    fun ensureChannels(c: Context) {
        val nm = c.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("say_main", "تنبيهات سَعْي", NotificationManager.IMPORTANCE_HIGH))
        nm.createNotificationChannel(NotificationChannel("say_quiet", "تنبيه الفجر الهادئ", NotificationManager.IMPORTANCE_LOW))
        val fs = NotificationChannel("say_fs", "الصلاة والقفل", NotificationManager.IMPORTANCE_HIGH)
        fs.setSound(null, null)
        nm.createNotificationChannel(fs)
        val w = NotificationChannel("say_wake", "منبه الاستيقاظ", NotificationManager.IMPORTANCE_HIGH)
        w.setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
        )
        w.enableVibration(true)
        nm.createNotificationChannel(w)
    }

    fun notify(c: Context, ch: String, id: Int, title: String, text: String) {
        val open = PendingIntent.getActivity(c, 0, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val n = Notification.Builder(c, ch)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title).setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setContentIntent(open).setAutoCancel(true).build()
        c.getSystemService(NotificationManager::class.java).notify(id, n)
    }

    fun scheduleAll(c: Context) {
        ensureChannels(c)
        val am = c.getSystemService(AlarmManager::class.java)
        val zone = ZoneId.systemDefault()
        val off = Prefs.offsets(c)
        for (dd in 0..2) {
            val date = LocalDate.now(zone).plusDays(dd.toLong())
            val h = PrayerTimes.hours(date.year, date.monthValue, date.dayOfMonth, off)
            val dow = date.dayOfWeek
            for (i in 0..4) {
                val t = PrayerTimes.at(date, h[i]).toInstant().toEpochMilli()
                if (i == 0) {
                    if (dow !in Notifs.SCHOOL) set(c, am, "fajr", date, i, t, "الفجر", "حان وقت صلاة الفجر. استعد وتوضأ.", 0)
                } else {
                    set(c, am, "prayer", date, i, t - 10_000, NAMES[i], "", t)
                }
            }
            Notifs.list.forEachIndexed { k, n ->
                if (n.days == null || dow in n.days) {
                    val tm = date.atTime(n.h, n.m).atZone(zone).toInstant().toEpochMilli()
                    set(c, am, if (n.wake) "wake" else "notif", date, 100 + k, tm, n.title, n.text, 0)
                }
            }
        }
    }

    private fun set(c: Context, am: AlarmManager, type: String, date: LocalDate, idx: Int, trig: Long, title: String, text: String, at: Long) {
        if (trig <= System.currentTimeMillis()) return
        val rc = (date.toEpochDay() * 1000 + idx).toInt()
        val i = Intent(c, AlarmReceiver::class.java)
            .putExtra("type", type).putExtra("title", title).putExtra("text", text).putExtra("id", rc).putExtra("at", at)
        val pi = PendingIntent.getBroadcast(c, rc, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val show = PendingIntent.getActivity(c, 0, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        am.setAlarmClock(AlarmManager.AlarmClockInfo(trig, show), pi)
    }
}
