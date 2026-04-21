package com.afelix.rifaapp.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val format = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO"))

    fun format(timestamp: Long): String {
        return format.format(Date(timestamp))
    }
}
