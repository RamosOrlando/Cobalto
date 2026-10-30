package com.cobalto.core.common.util

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

fun getCurrentDateStr(): String {
    val currentMoment = Clock.System.now()
    val localDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = localDateTime.day.toString().padStart(2, '0')
    val month = localDateTime.month.number.toString().padStart(2, '0')
    val year = localDateTime.year
    return "$day/$month/$year"
}

fun getCurrentIsoDate(): String {
    return Clock.System.now().toString()
}

fun formatDateToDisplay(dateStr: String?): String {
    if (dateStr == null) return "---"
    
    // Attempt to parse dd-MM-yyyy or yyyy-MM-dd
    val parts = if (dateStr.contains("-")) {
        dateStr.split("-")
    } else if (dateStr.contains("/")) {
        dateStr.split("/")
    } else {
        return dateStr
    }

    if (parts.size != 3) return dateStr

    val day: String
    val monthNum: Int
    val year: String

    if (parts[0].length == 4) {
        // yyyy-MM-dd
        year = parts[0]
        monthNum = parts[1].toIntOrNull() ?: return dateStr
        day = parts[2].padStart(2, '0')
    } else {
        // dd-MM-yyyy
        day = parts[0].padStart(2, '0')
        monthNum = parts[1].toIntOrNull() ?: return dateStr
        year = parts[2]
    }

    val monthName = when (monthNum) {
        1 -> "Ene"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Abr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Ago"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dic"
        else -> monthNum.toString()
    }

    return "$day-$monthName-$year"
}
