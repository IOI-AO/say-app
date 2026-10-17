package com.say.app

import java.time.DayOfWeek
import java.time.DayOfWeek.*

object Notifs {
    val SCHOOL = setOf(WEDNESDAY, THURSDAY)
    val BAGN = setOf(TUESDAY, WEDNESDAY)
    val MATH = setOf(SATURDAY, MONDAY, THURSDAY)

    class N(val title: String, val days: Set<DayOfWeek>?, val h: Int, val m: Int, val text: String, val wake: Boolean = false)

    val list = listOf(
        N("الاستيقاظ المدرسي", SCHOOL, 5, 15, "يا أسطى اصحى! وضوء وفجر والمسجد، وتجمع المدرسة 7:45 ص.", true),
        N("الحديد بعد الغداء", null, 14, 45, "ما تنساش جرعة الحديد حسب تعليمات الدكتور 💊"),
        N("حصة الرياضيات", MATH, 15, 20, "الحصة الساعة 4:00. جهّز كشكولك وانزل في ميعادك."),
        N("مذاكرة بعد الظهر", setOf(SUNDAY, TUESDAY, WEDNESDAY), 17, 0, "جاهز لجلستك الأساسية؟ افتح التطبيق وشغّل التركيز."),
        N("مذاكرة الجمعة", setOf(FRIDAY), 16, 0, "ساعتين مذاكرة بعد الظهر: قسّمهم بين الرياضيات والإنجليزي."),
        N("مذاكرة بعد الحصة", MATH, 18, 45, "خلصت الحصة؟ ابدأ جلسة المذاكرة وشغّل التركيز."),
        N("الجيم الليلة", setOf(SATURDAY, MONDAY), 21, 30, "جيم الليلة. تأكد إنك أكلت وارتحت، ولو حسيت بدوار متتمرنش."),
        N("الجيم بعد المدرسة", setOf(WEDNESDAY), 17, 0, "الجيم بعد المدرسة لو طاقتك تسمح. لو في دوار متتمرنش."),
        N("موجز اليوم (ليلة مدرسة)", BAGN, 20, 30, "اكتب موجز اليوم دلوقتي قبل الشنطة والنوم."),
        N("موجز اليوم", setOf(SATURDAY, SUNDAY, MONDAY, THURSDAY, FRIDAY), 23, 30, "يومك خلص يا بطل، اكتب موجز اليوم عشان نظبط بكرة."),
        N("شنطة المدرسة", BAGN, 21, 0, "جهّز شنطة المدرسة والملابس دلوقتي. ميعاد نومك 9:15."),
        N("ميعاد النوم", BAGN, 21, 15, "ميعاد النوم 9:15 يا أسطى. اقفل الموبايل. هتصحى 5:15 للمدرسة."),
        N("مراجعة الأسبوع", setOf(FRIDAY), 20, 0, "راجع إنجازات الأسبوع وحدد أولويات الأسبوع الجديد.")
    )
}
