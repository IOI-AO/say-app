package com.say.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

class BlockService : AccessibilityService() {
    companion object {
        val ALWAYS_OK = setOf(
            "com.android.systemui", "android", "com.android.dialer", "com.google.android.dialer",
            "com.samsung.android.dialer", "com.samsung.android.incallui", "com.android.incallui",
            "com.android.server.telecom", "com.android.phone", "com.android.emergency"
        )
        val FOCUS_BLOCK = setOf(
            "com.instagram.android", "com.facebook.katana", "com.facebook.lite", "com.facebook.orca",
            "com.zhiliaoapp.musically", "com.ss.android.ugc.trill", "com.snapchat.android",
            "com.twitter.android", "com.google.android.youtube", "com.reddit.frontpage",
            "com.activision.callofduty.shooter", "com.tencent.ig", "com.pubg.imobile",
            "com.dts.freefireth", "com.dts.freefiremax", "com.supercell.clashofclans",
            "com.supercell.brawlstars", "com.roblox.client", "com.mojang.minecraftpe",
            "com.innersloth.spacemafia", "com.netflix.mediaclient", "com.kwai.video",
            "tv.twitch.android.app", "com.discord", "com.pinterest"
        )
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent?) {
        if (e == null || e.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = e.packageName?.toString() ?: return
        val type = Lock.type(this)
        if (type.isEmpty() || !shouldBlock(pkg, type)) return
        try {
            startActivity(
                Intent(this, LockActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            )
        } catch (x: Exception) { }
    }

    private fun shouldBlock(pkg: String, type: String): Boolean {
        if (pkg == packageName || pkg in ALWAYS_OK) return false
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        if (am.mode != AudioManager.MODE_NORMAL) return false
        if (pkg == launcherPkg() || pkg == imePkg()) return false
        return if (type == "prayer") true else (pkg in FOCUS_BLOCK || pkg in Prefs.extra(this))
    }

    private fun launcherPkg(): String? = try {
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        packageManager.resolveActivity(i, 0)?.activityInfo?.packageName
    } catch (x: Exception) { null }

    private fun imePkg(): String? =
        Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)?.substringBefore('/')

    override fun onInterrupt() {}
}
