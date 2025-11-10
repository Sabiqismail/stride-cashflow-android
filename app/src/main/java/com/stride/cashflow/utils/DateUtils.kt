package com.stride.cashflow.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// This function takes a string like "2025-11" and returns "November, 2025"
@RequiresApi(Build.VERSION_CODES.O)
fun formatMonthString(monthString: String): String {
    return try {
        val yearMonth = YearMonth.parse(monthString, DateTimeFormatter.ofPattern("yyyy-MM"))
        yearMonth.format(DateTimeFormatter.ofPattern("MMMM, yyyy", Locale.getDefault()))
    } catch (e: Exception) {
        // If parsing fails for any reason, just return the original string
        monthString
    }
}