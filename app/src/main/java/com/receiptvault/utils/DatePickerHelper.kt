package com.receiptvault.utils

import android.app.DatePickerDialog
import android.content.Context
import android.widget.EditText
import java.util.Calendar

object DatePickerHelper {

    /**
     * Shows a MaterialDatePicker and populates [target] EditText with the selected date.
     * Format: yyyy-MM-dd
     */
    fun showDatePicker(context: Context, target: EditText) {
        val cal = Calendar.getInstance()

        // Pre-fill from existing text if parseable
        runCatching {
            val existing = target.text.toString()
            if (existing.isNotBlank()) {
                val parts = existing.split("-")
                if (parts.size == 3) {
                    cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                }
            }
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formatted = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                target.setText(formatted)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
