package com.example.ui.util

/**
 * Centralized input validation utilities for TuitionOS.
 * All validation rules are defined here for consistency
 * across UI dialogs and repository layer.
 */
object ValidationUtils {

    // =============================================
    // CHARACTER LIMITS
    // =============================================
    const val MAX_NAME_LENGTH = 100
    const val MAX_STANDARD_LENGTH = 20
    const val MAX_BATCH_NAME_LENGTH = 50
    const val MAX_SUBJECT_LENGTH = 50
    const val MAX_PHONE_LENGTH = 15
    const val MAX_ROLE_LENGTH = 50
    const val MAX_DESCRIPTION_LENGTH = 500
    const val MAX_SOURCE_LENGTH = 50
    const val MAX_UPI_LENGTH = 50
    const val MAX_ORG_NAME_LENGTH = 100
    const val MAX_PLAN_LENGTH = 30
    const val MAX_MONTH_LENGTH = 30
    const val MAX_INSTALLMENT_LENGTH = 50
    const val MAX_AMOUNT_LENGTH = 12

    // =============================================
    // PHONE VALIDATION (Indian mobile: 10 digits, starts with 6-9)
    // =============================================
    private val INDIAN_PHONE_REGEX = Regex("^[6-9]\\d{9}$")

    /**
     * Validates an Indian mobile number.
     * Accepts 10-digit numbers starting with 6-9.
     * Also accepts empty phone (for optional fields).
     */
    fun isValidPhone(phone: String): Boolean {
        val digits = phone.replace(Regex("[\\s\\-+()]"), "")
        if (digits.isEmpty()) return true  // optional field
        // Strip leading "91" or "0" if present
        val clean = when {
            digits.startsWith("91") && digits.length == 12 -> digits.drop(2)
            digits.startsWith("0") && digits.length == 11 -> digits.drop(1)
            else -> digits
        }
        return INDIAN_PHONE_REGEX.matches(clean)
    }

    /** Phone is required (non-empty + valid format) */
    fun isValidPhoneRequired(phone: String): Boolean {
        val digits = phone.replace(Regex("[\\s\\-+()]"), "")
        if (digits.isEmpty()) return false
        val clean = when {
            digits.startsWith("91") && digits.length == 12 -> digits.drop(2)
            digits.startsWith("0") && digits.length == 11 -> digits.drop(1)
            else -> digits
        }
        return INDIAN_PHONE_REGEX.matches(clean)
    }

    // =============================================
    // EMAIL VALIDATION
    // =============================================
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return true  // optional
        return EMAIL_REGEX.matches(email.trim())
    }

    fun isValidEmailRequired(email: String): Boolean {
        if (email.isBlank()) return false
        return EMAIL_REGEX.matches(email.trim())
    }

    // =============================================
    // AMOUNT VALIDATION
    // =============================================
    fun isValidAmount(amount: String): Boolean {
        if (amount.isBlank()) return false
        val value = amount.toDoubleOrNull() ?: return false
        return value >= 0.0 && value <= 99_99_99_999.0  // max 99 crore
    }

    fun parseAmount(amount: String, default: Double = 0.0): Double {
        return amount.toDoubleOrNull() ?: default
    }

    // =============================================
    // DATE VALIDATION (YYYY-MM-DD)
    // =============================================
    private val DATE_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    fun isValidDate(date: String): Boolean {
        if (date.isBlank()) return true  // optional
        if (!DATE_REGEX.matches(date)) return false
        return try {
            val parts = date.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            year in 2020..2100 && month in 1..12 && day in 1..31
        } catch (_: Exception) { false }
    }

    // =============================================
    // API KEY VALIDATION
    // =============================================
    private val API_KEY_REGEX = Regex("^[A-Za-z0-9_-]{16,128}$")

    fun isValidApiKey(key: String): Boolean {
        if (key.isBlank()) return true  // optional
        return API_KEY_REGEX.matches(key.trim())
    }

    // =============================================
    // REQUIRED FIELD VALIDATION
    // =============================================
    fun isNotBlank(value: String): Boolean = value.isNotBlank()

    // =============================================
    // ERROR MESSAGE HELPERS
    // =============================================
    fun phoneError(phone: String): String? {
        if (phone.isEmpty()) return null
        return if (!isValidPhone(phone)) "Enter valid 10-digit Indian mobile (e.g. 9876543210)" else null
    }

    fun phoneRequiredError(phone: String): String? {
        if (phone.isBlank()) return "Phone number is required"
        return if (!isValidPhoneRequired(phone)) "Enter valid 10-digit Indian mobile (e.g. 9876543210)" else null
    }

    fun requiredFieldError(value: String, fieldName: String): String? {
        return if (value.isBlank()) "$fieldName is required" else null
    }

    fun maxLengthError(value: String, max: Int, fieldName: String): String? {
        return if (value.length > max) "$fieldName must be $max characters or less" else null
    }

    fun amountError(amount: String): String? {
        if (amount.isBlank()) return "Amount is required"
        if (amount.toDoubleOrNull() == null) return "Enter a valid number"
        if (!isValidAmount(amount)) return "Amount must be between 0 and 99,99,99,999"
        return null
    }

    fun dateError(date: String): String? {
        if (date.isBlank()) return null
        if (!isValidDate(date)) return "Enter valid date in YYYY-MM-DD format"
        return null
    }

    // =============================================
    // UPI ID VALIDATION
    // =============================================
    private val UPI_REGEX = Regex("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")

    fun isValidUpi(upi: String): Boolean {
        if (upi.isBlank()) return true  // optional
        return UPI_REGEX.matches(upi.trim())
    }

    fun upiError(upi: String): String? {
        if (upi.isBlank()) return null
        return if (!isValidUpi(upi)) "Enter valid UPI ID (e.g. yourname@bank)" else null
    }
}
