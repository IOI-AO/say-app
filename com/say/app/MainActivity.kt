package com.say.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {
    private lateinit var wv: WebView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.statusBarColor = 0xFF121212.toInt()
        window.navigationBarColor = 0xFF0D0D0C.toInt()
        wv = WebView(this)
        wv.setBackgroundColor(0xFF121212.toInt())
        val root = FrameLayout(this)
        root.fitsSystemWindows = true
        root.addView(wv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setContentView(root)
        wv.settings.javaScriptEnabled = true
        wv.settings.domStorageEnabled = true
        wv.addJavascriptInterface(Bridge(), "Android")
        wv.webViewClient = WebViewClient()
        wv.loadUrl("file:///android_asset/index.html")
        Scheduler.scheduleAll(this)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
    }

    override fun onBackPressed() { moveTaskToBack(true) }

    inner class Bridge {
        private val c: Activity get() = this@MainActivity

        @JavascriptInterface fun prayers(y: Int, m: Int, d: Int): String =
            PrayerTimes.hours(y, m, d, Prefs.offsets(c)).joinToString(",", "[", "]")

        @JavascriptInterface fun getOffsets(): String = Prefs.offsetsCsv(c)

        @JavascriptInterface fun setOffsets(csv: String) { Prefs.setOffsets(c, csv); Scheduler.scheduleAll(c) }

        @JavascriptInterface fun setExtraBlocked(csv: String) { Prefs.setExtra(c, csv) }

        @JavascriptInterface fun startFocus(sec: Int) {
            Lock.set(c, "focus", "تركيز", 0, System.currentTimeMillis() + sec * 1000L)
        }

        @JavascriptInterface fun stopFocus() { if (Lock.type(c) == "focus") Lock.clear(c) }

        @JavascriptInterface fun notifList(): String {
            val a = JSONArray()
            a.put("🕌 الظهر/العصر/المغرب/العشاء: عداد 10 ثواني ثم الأذان ثم قفل 15 دقيقة")
            a.put("🔔 الفجر: تنبيه هادئ (ما عدا الأربعاء والخميس، منبه 5:15 بدله)")
            Notifs.list.forEach { a.put("🔔 " + it.title + " — " + String.format("%02d:%02d", it.h, it.m)) }
            return a.toString()
        }

        @JavascriptInterface fun status(): String {
            val en = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
            val notif = if (Build.VERSION.SDK_INT >= 33)
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED else true
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            return JSONObject()
                .put("a11y", en.contains("$packageName/"))
                .put("overlay", Settings.canDrawOverlays(c))
                .put("notif", notif)
                .put("battery", pm.isIgnoringBatteryOptimizations(packageName))
                .toString()
        }

        @JavascriptInterface fun openA11y() { runOnUiThread { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } }

        @JavascriptInterface fun openOverlay() {
            runOnUiThread { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) }
        }

        @JavascriptInterface fun openBattery() {
            runOnUiThread { startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName"))) }
        }

        @JavascriptInterface fun reqNotif() {
            runOnUiThread {
                if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        @JavascriptInterface fun claude(id: Int, key: String, body: String) {
            Thread {
                var status = 0
                var out = ""
                try {
                    val cn = URL("https://api.anthropic.com/v1/messages").openConnection() as HttpURLConnection
                    cn.requestMethod = "POST"
                    cn.connectTimeout = 20000
                    cn.readTimeout = 60000
                    cn.doOutput = true
                    cn.setRequestProperty("content-type", "application/json")
                    cn.setRequestProperty("x-api-key", key)
                    cn.setRequestProperty("anthropic-version", "2023-06-01")
                    cn.outputStream.use { it.write(body.toByteArray()) }
                    status = cn.responseCode
                    val st = if (status in 200..299) cn.inputStream else cn.errorStream
                    out = st?.bufferedReader()?.readText() ?: ""
                } catch (e: Exception) {
                    status = 0
                    out = e.message ?: "network error"
                }
                runOnUiThread { wv.evaluateJavascript("window.onClaude($id,$status,${JSONObject.quote(out)})", null) }
            }.start()
        }
    }
}
