package com.receiptvault.utils

import org.junit.Assert.*
import org.junit.Test

class ReceiptValidatorTest {

    @Test
    fun `valid input passes validation`() {
        val result = ReceiptValidator.validate("Starbucks", "2024-01-01", "12.50", "Food & Dining")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    @Test
    fun `blank merchant fails validation`() {
        val result = ReceiptValidator.validate("", "2024-01-01", "12.50", "Food & Dining")
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun `blank date fails validation`() {
        val result = ReceiptValidator.validate("Starbucks", "", "12.50", "Food & Dining")
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun `invalid amount string fails validation`() {
        val result = ReceiptValidator.validate("Starbucks", "2024-01-01", "abc", "Food & Dining")
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun `negative amount fails validation`() {
        val result = ReceiptValidator.validate("Starbucks", "2024-01-01", "-5.00", "Food & Dining")
        assertFalse(result.isValid)
    }

    @Test
    fun `amount over 1 million fails validation`() {
        val result = ReceiptValidator.validate("Starbucks", "2024-01-01", "2000000.00", "Food & Dining")
        assertFalse(result.isValid)
    }

    @Test
    fun `merchant too long fails validation`() {
        val longName = "A".repeat(101)
        val result = ReceiptValidator.validate(longName, "2024-01-01", "10.00", "Food & Dining")
        assertFalse(result.isValid)
    }

    @Test
    fun `zero amount is valid`() {
        val result = ReceiptValidator.validate("Starbucks", "2024-01-01", "0.00", "Food & Dining")
        assertTrue(result.isValid)
    }

    @Test
    fun `blank category fails validation`() {
        val result = ReceiptValidator.validate("Starbucks", "2024-01-01", "10.00", "")
        assertFalse(result.isValid)
    }
}
