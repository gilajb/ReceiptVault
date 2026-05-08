package com.receiptvault.utils

import java.util.regex.Pattern

object OcrParser {

    private val DATE_PATTERNS = listOf(
        Pattern.compile("""(\d{1,2})[/\-\.](\d{1,2})[/\-\.](\d{2,4})"""),
        Pattern.compile("""(\d{4})[/\-\.](\d{1,2})[/\-\.](\d{1,2})"""),
        Pattern.compile("""(\w{3,9})\s+(\d{1,2}),?\s+(\d{4})""")
    )

    private val AMOUNT_PATTERN = Pattern.compile(
        """(?:total|amount|due|balance|subtotal|grand\s*total)?[:\s]*[$£€¥]?\s*(\d{1,6}[.,]\d{2})""",
        Pattern.CASE_INSENSITIVE
    )

    private val CURRENCY_PATTERN = Pattern.compile("""[$£€¥]\s*(\d{1,6}[.,]\d{2})""")

    fun extractMerchant(blocks: List<String>): String {
        // Heuristic: first non-empty block that's not a date or number
        return blocks.firstOrNull { block ->
            block.isNotBlank()
                && block.length > 2
                && !block.matches(Regex("""[\d.,\s$£€¥]+"""))
                && !block.contains(Regex("""\d{1,2}[/\-\.]\d{1,2}"""))
        }?.trim()?.take(60) ?: "Unknown Merchant"
    }

    fun extractDate(fullText: String): String {
        for (pattern in DATE_PATTERNS) {
            val matcher = pattern.matcher(fullText)
            if (matcher.find()) {
                return matcher.group(0) ?: continue
            }
        }
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }

    fun extractAmount(fullText: String): Double {
        // First try labeled "total" patterns
        val totalMatcher = AMOUNT_PATTERN.matcher(fullText)
        val candidates = mutableListOf<Double>()

        while (totalMatcher.find()) {
            totalMatcher.group(1)?.replace(",", ".")?.toDoubleOrNull()?.let {
                candidates.add(it)
            }
        }

        if (candidates.isNotEmpty()) return candidates.max()

        // Fallback: largest currency value
        val currencyMatcher = CURRENCY_PATTERN.matcher(fullText)
        while (currencyMatcher.find()) {
            currencyMatcher.group(1)?.replace(",", ".")?.toDoubleOrNull()?.let {
                candidates.add(it)
            }
        }

        return candidates.maxOrNull() ?: 0.0
    }

    fun guessCategory(merchant: String, fullText: String): String {
        val combined = (merchant + " " + fullText).lowercase()
        return when {
            combined.containsAny("restaurant", "cafe", "coffee", "pizza", "burger",
                "grill", "diner", "sushi", "food", "eat", "kitchen") -> "Food & Dining"
            combined.containsAny("supermarket", "grocery", "market", "walmart",
                "kroger", "costco", "whole foods") -> "Groceries"
            combined.containsAny("uber", "lyft", "taxi", "bus", "train",
                "transit", "fuel", "gas", "parking") -> "Transport"
            combined.containsAny("amazon", "ebay", "shop", "store", "mall",
                "clothing", "fashion", "apparel") -> "Shopping"
            combined.containsAny("hospital", "clinic", "pharmacy", "drug",
                "health", "medical", "doctor") -> "Healthcare"
            combined.containsAny("electric", "water", "internet", "phone",
                "utility", "bill", "rent") -> "Utilities"
            combined.containsAny("cinema", "theater", "netflix", "game",
                "entertainment", "ticket", "sport") -> "Entertainment"
            combined.containsAny("hotel", "airbnb", "flight", "airline",
                "travel", "booking") -> "Travel"
            else -> "Other"
        }
    }

    private fun String.containsAny(vararg keywords: String) =
        keywords.any { this.contains(it) }

    val CATEGORIES = listOf(
        "All", "Food & Dining", "Groceries", "Transport", "Shopping",
        "Healthcare", "Utilities", "Entertainment", "Travel", "Other"
    )
}
