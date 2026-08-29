package util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

fun hoje(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
