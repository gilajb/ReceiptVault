package com.receiptvault.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun Double.toCurrencyString(): String {
    return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(this)
}

fun String.toDisplayDate(): String {
    return try {
        val inputFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        )
        var date: Date? = null
        for (fmt in inputFormats) {
            date = runCatching { fmt.parse(this) }.getOrNull()
            if (date != null) break
        }
        date?.let {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
        } ?: this
    } catch (e: Exception) {
        this
    }
}

fun currentMonthPrefix(): String {
    return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
}

fun monthDisplayName(prefix: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(prefix) ?: return prefix
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        prefix
    }
}

fun previousMonth(prefix: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val date = sdf.parse(prefix) ?: return prefix
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.MONTH, -1)
        sdf.format(cal.time)
    } catch (e: Exception) {
        prefix
    }
}

fun nextMonth(prefix: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val date = sdf.parse(prefix) ?: return prefix
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.MONTH, 1)
        sdf.format(cal.time)
    } catch (e: Exception) {
        prefix
    }
}

fun sanitizeInput(input: String): String {
    return input.trim()
        .replace(Regex("[<>\"'%;()&+]"), "")
        .take(500)
}
