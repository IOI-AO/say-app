package com.say.app

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class LockActivity : Activity() {
    private val h = Handler(Looper.getMainLooper())
    private lateinit var big: TextView
    private lateinit var msg: TextView
    private lateinit var sub: TextView
    private lateinit var back: Button
    private var tone: ToneGenerator? = null
    private var last = -1
    private val tick = object : Runnable {
        override fun run() { update(); h.postDelayed(this, 250) }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true) }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.statusBarColor = 0xFF0B0B0A.toInt()
        window.navigationBarColor = 0xFF0B0B0A.toInt()
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setBackgroundColor(0xFF0B0B0A.toInt())
        root.setPadding(48, 48, 48, 48)
        root.layoutDirection = View.LAYOUT_DIRECTION_RTL
        fun tv(sz: Float, col: Int): TextView {
            val t = TextView(this)
            t.textSize = sz
            t.setTextColor(col)
            t.gravity = Gravity.CENTER
            root.addView(t)
            return t
        }
        msg = tv(22f, 0xFFECE8DC.toInt())
        big = tv(96f, 0xFFE3AB2C.toInt())
        sub = tv(16f, 0xFF9A9786.toInt())
        val dial = Button(this)
        dial.text = "📞 الهاتف"
        dial.setOnClickListener { startActivity(Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        root.addView(dial)
        back = Button(this)
        back.text = "رجوع للمذاكرة"
        back.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
        root.addView(back)
        setContentView(root)
        try { tone = ToneGenerator(AudioManager.STREAM_ALARM, 70) } catch (e: Exception) { }
    }

    override fun onResume() { super.onResume(); h.post(tick) }
    override fun onPause() { super.onPause(); h.removeCallbacks(tick) }
    override fun onDestroy() { try { tone?.release() } catch (e: Exception) { }; super.onDestroy() }

    private fun mmss(ms: Long): String {
        val s = maxOf(0L, ms / 1000)
        return String.format("%02d:%02d", s / 60, s % 60)
    }

    private fun update() {
        val type = Lock.type(this)
        if (type.isEmpty()) { finish(); return }
        val now = System.currentTimeMillis()
        val end = Lock.end(this)
        if (type == "prayer") {
            val at = Lock.adhanAt(this)
            val name = Lock.name(this)
            back.visibility = View.GONE
            if (now < at) {
                val s = Math.ceil((at - now) / 1000.0).toInt()
                big.text = s.toString()
                msg.text = "الأذان بعد ثواني، استعد للصلاة"
                sub.text = "صلاة $name"
                if (s != last) { last = s; try { tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 120) } catch (e: Exception) { } }
            } else {
                big.text = mmss(end - now)
                msg.text = "🕌 حان وقت صلاة $name"
                sub.text = "التطبيقات مقفولة لأداء الصلاة، وهتفتح تلقائي بعد ربع ساعة"
            }
        } else {
            big.text = mmss(end - now)
            msg.text = "جلسة تركيز"
            sub.text = "التطبيقات المشتتة مقفولة. المكالمات شغالة."
            back.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        if (Lock.type(this) == "focus") moveTaskToBack(true)
    }
}
