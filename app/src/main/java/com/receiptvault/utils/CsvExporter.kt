package com.receiptvault.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.receiptvault.data.local.entities.Receipt
import java.io.File
import java.io.FileWriter
import java.io.OutputStream

object CsvExporter {

    fun exportReceipts(context: Context, receipts: List<Receipt>): Uri? {
        val csvContent = buildCsvContent(receipts)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(context, csvContent)
        } else {
            saveToLegacyStorage(context, csvContent)
        }
    }

    private fun buildCsvContent(receipts: List<Receipt>): String {
        val sb = StringBuilder()
        sb.appendLine("Merchant,Date,Amount,Category,Notes")
        receipts.forEach { receipt ->
            sb.appendLine(
                "${csvEscape(receipt.merchant)}," +
                "${csvEscape(receipt.date)}," +
                "${receipt.amount}," +
                "${csvEscape(receipt.category)}," +
                "${csvEscape(receipt.notes ?: "")}"
            )
        }
        return sb.toString()
    }

    private fun csvEscape(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
    }

    private fun saveViaMediaStore(context: Context, content: String): Uri? {
        val fileName = "receipts_${System.currentTimeMillis()}.csv"
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        resolver.openOutputStream(uri)?.use { stream ->
            stream.write(content.toByteArray())
        }
        return uri
    }

    private fun saveToLegacyStorage(context: Context, content: String): Uri? {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        downloadsDir.mkdirs()
        val file = File(downloadsDir, "receipts_${System.currentTimeMillis()}.csv")
        FileWriter(file).use { it.write(content) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun createShareIntent(context: Context, uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Receipt Vault Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
