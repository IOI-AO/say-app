package com.say.app

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class PrayerService : Service() {
    private val h = Handler(Looper.getMainLooper())
    private var mp: MediaPlayer? = null

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int {
        val name = i?.getStringExtra("name") ?: "الصلاة"
        val at = i?.getLongExtra("adhanAt", System.currentTimeMillis()) ?: System.currentTimeMillis()
        val end = at + 15 * 60_000L
        h.removeCallbacksAndMessages(null)
        Lock.set(this, "prayer", name, at, end)
        Scheduler.ensureChannels(this)
        val pi = PendingIntent.getActivity(this, 1, Intent(this, LockActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val n = Notification.Builder(this, "say_fs")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentTitle("صلاة $name").setContentText("التطبيقات مقفولة لربع ساعة")
            .setFullScreenIntent(pi, true).setOngoing(true).build()
        try {
            if (Build.VERSION.SDK_INT >= 29) startForeground(77, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            else startForeground(77, n)
        } catch (e: Exception) { }
        try {
            startActivity(Intent(this, LockActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        } catch (e: Exception) { }
        val now = System.currentTimeMillis()
        h.postDelayed({ playAdhan() }, maxOf(0L, at - now))
        h.postDelayed({
            Lock.clear(this)
            Scheduler.scheduleAll(this)
            stopSelf()
        }, maxOf(0L, end - now))
        return START_NOT_STICKY
    }

    private fun playAdhan() {
        try {
            val m = MediaPlayer()
            m.setAudioAttributes(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            )
            val id = resources.getIdentifier("adhan", "raw", packageName)
            if (id != 0) {
                val afd = resources.openRawResourceFd(id)
                m.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
            } else {
                m.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                h.postDelayed({ try { m.stop() } catch (e: Exception) { } }, 25000)
            }
            m.setOnCompletionListener { it.release(); if (mp === it) mp = null }
            m.prepare()
            m.start()
            mp = m
        } catch (e: Exception) { }
    }

    override fun onDestroy() {
        h.removeCallbacksAndMessages(null)
        try { mp?.release() } catch (e: Exception) { }
        super.onDestroy()
    }
}
