package com.afelix.rifaapp.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateFormatter {
    private val format = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO")).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun format(timestamp: Long): String {
        return format.format(Date(timestamp))
    }
}
