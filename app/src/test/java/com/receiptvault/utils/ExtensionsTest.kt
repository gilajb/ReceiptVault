package com.receiptvault.utils

import org.junit.Assert.*
import org.junit.Test

class ExtensionsTest {

    @Test
    fun `sanitizeInput trims whitespace`() {
        val result = sanitizeInput("  hello  ")
        assertEquals("hello", result)
    }

    @Test
    fun `sanitizeInput removes dangerous characters`() {
        val result = sanitizeInput("<script>alert('xss')</script>")
        assertFalse(result.contains("<"))
        assertFalse(result.contains(">"))
    }

    @Test
    fun `sanitizeInput truncates at 500 chars`() {
        val longInput = "a".repeat(600)
        val result = sanitizeInput(longInput)
        assertEquals(500, result.length)
    }

    @Test
    fun `currentMonthPrefix returns yyyy-MM format`() {
        val prefix = currentMonthPrefix()
        assertTrue("Should match yyyy-MM", prefix.matches(Regex("""\d{4}-\d{2}""")))
    }

    @Test
    fun `previousMonth decrements month correctly`() {
        val prev = previousMonth("2024-03")
        assertEquals("2024-02", prev)
    }

    @Test
    fun `previousMonth handles year boundary`() {
        val prev = previousMonth("2024-01")
        assertEquals("2023-12", prev)
    }

    @Test
    fun `nextMonth increments month correctly`() {
        val next = nextMonth("2024-03")
        assertEquals("2024-04", next)
    }

    @Test
    fun `nextMonth handles year boundary`() {
        val next = nextMonth("2023-12")
        assertEquals("2024-01", next)
    }

    @Test
    fun `monthDisplayName formats correctly`() {
        val name = monthDisplayName("2024-03")
        assertTrue("Should contain March", name.contains("March") || name.contains("Mar"))
        assertTrue("Should contain 2024", name.contains("2024"))
    }
}
