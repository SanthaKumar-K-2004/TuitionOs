package com.example.ui.util

/**
 * Input validation for all forms in the app.
 * Returns ValidationResult with success/error state.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

object InputValidators {

    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) return ValidationResult(false, "Email is required")
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        if (!email.matches(emailRegex.toRegex())) {
            return ValidationResult(false, "Enter a valid email address")
        }
        return ValidationResult(true)
    }

    fun validatePhone(phone: String): ValidationResult {
        if (phone.isBlank()) return ValidationResult(false, "Phone number is required")
        val cleanPhone = phone.replace(" ", "").replace("+", "").replace("-", "")
        if (cleanPhone.length < 10) {
            return ValidationResult(false, "Enter a valid 10-digit phone number")
        }
        if (cleanPhone.length > 12) {
            return ValidationResult(false, "Phone number is too long")
        }
        if (!cleanPhone.all { it.isDigit() }) {
            return ValidationResult(false, "Phone must contain only digits")
        }
        return ValidationResult(true)
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) return ValidationResult(false, "Password is required")
        if (password.length < 6) {
            return ValidationResult(false, "Password must be at least 6 characters")
        }
        return ValidationResult(true)
    }

    fun validateAmount(amount: String): ValidationResult {
        if (amount.isBlank()) return ValidationResult(false, "Amount is required")
        val value = amount.toDoubleOrNull()
            ?: return ValidationResult(false, "Enter a valid number")
        if (value <= 0) {
            return ValidationResult(false, "Amount must be greater than zero")
        }
        if (value > 999999) {
            return ValidationResult(false, "Amount exceeds maximum limit")
        }
        return ValidationResult(true)
    }

    fun validateRequired(value: String, fieldName: String): ValidationResult {
        if (value.isBlank()) {
            return ValidationResult(false, "$fieldName is required")
        }
        if (value.length > 100) {
            return ValidationResult(false, "$fieldName is too long (max 100 characters)")
        }
        return ValidationResult(true)
    }

    fun validateDate(date: String): ValidationResult {
        if (date.isBlank()) return ValidationResult(false, "Date is required")
        val dateRegex = "^\\d{4}-\\d{2}-\\d{2}$"
        if (!date.matches(dateRegex.toRegex())) {
            return ValidationResult(false, "Date must be in YYYY-MM-DD format")
        }
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            sdf.isLenient = false
            sdf.parse(date)
            ValidationResult(true)
        } catch (e: Exception) {
            ValidationResult(false, "Invalid date")
        }
    }
}
