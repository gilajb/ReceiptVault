package com.receiptvault.utils

import org.junit.Assert.*
import org.junit.Test

class OcrParserTest {

    @Test
    fun `extractAmount returns largest currency value`() {
        val text = "Subtotal $12.50\nTax $1.25\nTotal $13.75"
        val amount = OcrParser.extractAmount(text)
        assertEquals(13.75, amount, 0.001)
    }

    @Test
    fun `extractAmount returns 0 when no amount found`() {
        val text = "Thank you for shopping with us!"
        val amount = OcrParser.extractAmount(text)
        assertEquals(0.0, amount, 0.001)
    }

    @Test
    fun `extractDate finds slash format date`() {
        val text = "Date: 05/15/2024\nMerchant: Coffee Shop"
        val date = OcrParser.extractDate(text)
        assertTrue("Date should contain 05", date.contains("05"))
    }

    @Test
    fun `extractDate finds dash format date`() {
        val text = "2024-03-20 Receipt"
        val date = OcrParser.extractDate(text)
        assertTrue("Date should contain 2024", date.contains("2024"))
    }

    @Test
    fun `extractMerchant returns first meaningful block`() {
        val blocks = listOf("STARBUCKS COFFEE", "123 Main Street", "Date: 2024-01-01")
        val merchant = OcrParser.extractMerchant(blocks)
        assertEquals("STARBUCKS COFFEE", merchant)
    }

    @Test
    fun `extractMerchant handles empty blocks`() {
        val blocks = listOf("", "   ", "WALMART")
        val merchant = OcrParser.extractMerchant(blocks)
        assertEquals("WALMART", merchant)
    }

    @Test
    fun `guessCategory identifies food restaurants`() {
        val category = OcrParser.guessCategory("Pizza Palace", "pepperoni pizza dinner")
        assertEquals("Food & Dining", category)
    }

    @Test
    fun `guessCategory identifies grocery stores`() {
        val category = OcrParser.guessCategory("Whole Foods Market", "organic vegetables milk eggs")
        assertEquals("Groceries", category)
    }

    @Test
    fun `guessCategory returns Other for unknown`() {
        val category = OcrParser.guessCategory("ACME Corp", "miscellaneous item 42")
        assertEquals("Other", category)
    }

    @Test
    fun `guessCategory identifies transport`() {
        val category = OcrParser.guessCategory("Uber Technologies", "ride fare")
        assertEquals("Transport", category)
    }

    @Test
    fun `CATEGORIES list has expected size`() {
        assertTrue("Should have at least 10 categories", OcrParser.CATEGORIES.size >= 10)
        assertTrue("First item should be All", OcrParser.CATEGORIES.first() == "All")
    }
}
