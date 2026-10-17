package com.say.app

import android.content.Context

object Prefs {
    fun sp(c: Context) = c.getSharedPreferences("say", Context.MODE_PRIVATE)
    fun offsetsCsv(c: Context): String = sp(c).getString("off", "0,2,0,1,0") ?: "0,2,0,1,0"
    fun offsets(c: Context): IntArray {
        val s = offsetsCsv(c).split(",")
        return IntArray(5) { s.getOrNull(it)?.trim()?.toIntOrNull() ?: 0 }
    }
    fun setOffsets(c: Context, csv: String) = sp(c).edit().putString("off", csv).apply()
    fun extra(c: Context): Set<String> =
        (sp(c).getString("extra", "") ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    fun setExtra(c: Context, csv: String) = sp(c).edit().putString("extra", csv).apply()
}

object Lock {
    fun set(c: Context, type: String, name: String, adhanAt: Long, end: Long) {
        Prefs.sp(c).edit().putString("lt", type).putString("ln", name).putLong("la", adhanAt).putLong("le", end).apply()
    }
    fun clear(c: Context) = Prefs.sp(c).edit().putString("lt", "").putLong("le", 0).apply()
    fun end(c: Context) = Prefs.sp(c).getLong("le", 0)
    fun type(c: Context): String {
        val t = Prefs.sp(c).getString("lt", "") ?: ""
        return if (end(c) > System.currentTimeMillis()) t else ""
    }
    fun adhanAt(c: Context) = Prefs.sp(c).getLong("la", 0)
    fun name(c: Context) = Prefs.sp(c).getString("ln", "") ?: ""
}
