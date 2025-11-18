@file:OptIn(ExperimentalTime::class)

package com.vincent.transfercloud.utils

import kotlinx.datetime.*
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlin.time.ExperimentalTime

private val monthDayFormatter = LocalDateTime.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED) // Nov, Dec,...
    char(' ')
	day(padding = Padding.ZERO)
}

fun formatIsoToMonthDay(isoString: String, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val instant = Instant.parse(isoString)
    val ldt = instant.toLocalDateTime(zone)
    return ldt.format(monthDayFormatter)
}