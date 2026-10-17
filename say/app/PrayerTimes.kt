package com.say.app

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.*

/** Asyut (27.18N, 31.18E), Egyptian General Authority: Fajr 19.5, Isha 17.5, Asr standard. */
object PrayerTimes {
    fun hours(y: Int, m: Int, d: Int, off: IntArray): DoubleArray {
        val r = Math.PI / 180
        val lat = 27.18 * r
        val lng = 31.18
        val date = LocalDate.of(y, m, d)
        val n = (date.toEpochDay() * 86400000.0 + 43200000.0 - 946728000000.0) / 86400000.0
        val lm = (280.46 + 0.9856474 * n) % 360
        val g = ((357.528 + 0.9856003 * n) % 360) * r
        val lam = (lm + 1.915 * sin(g) + 0.02 * sin(2 * g)) * r
        val ep = (23.439 - 4e-7 * n) * r
        val dec = asin(sin(ep) * sin(lam))
        val ra = atan2(cos(ep) * sin(lam), cos(lam)) / r
        var eq = lm - ra
        eq = ((eq + 180) % 360 + 360) % 360 - 180
        val tz = ZoneId.systemDefault().rules.getOffset(date.atTime(12, 0)).totalSeconds / 3600.0
        val noon = 12 - lng / 15 - eq / 15 + tz
        fun hh(a: Double) = acos((sin(a * r) - sin(lat) * sin(dec)) / (cos(lat) * cos(dec))) / r / 15
        val asrA = atan(1 / (1 + tan(abs(lat - dec)))) / r
        val t = doubleArrayOf(noon - hh(-19.5), noon, noon + hh(asrA), noon + hh(-0.833), noon + hh(-17.5))
        return DoubleArray(5) { t[it] + off[it] / 60.0 }
    }

    fun at(date: LocalDate, hours: Double): ZonedDateTime =
        date.atStartOfDay().plusSeconds((hours * 3600).toLong()).atZone(ZoneId.systemDefault())
}
