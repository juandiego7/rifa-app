package com.afelix.rifaapp.core.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    fun format(amount: Double): String {
        return format.format(amount)
    }
}
