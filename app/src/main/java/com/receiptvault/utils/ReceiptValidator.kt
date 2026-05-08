package com.receiptvault.utils

object ReceiptValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validate(
        merchant: String,
        date: String,
        amountStr: String,
        category: String
    ): ValidationResult {

        if (merchant.isBlank()) {
            return ValidationResult(false, "Merchant name is required")
        }

        if (merchant.length > 100) {
            return ValidationResult(false, "Merchant name is too long (max 100 chars)")
        }

        if (date.isBlank()) {
            return ValidationResult(false, "Date is required")
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount < 0) {
            return ValidationResult(false, "Please enter a valid positive amount")
        }

        if (amount > 1_000_000) {
            return ValidationResult(false, "Amount seems too large — please check")
        }

        if (category.isBlank()) {
            return ValidationResult(false, "Please select a category")
        }

        return ValidationResult(true)
    }
}
