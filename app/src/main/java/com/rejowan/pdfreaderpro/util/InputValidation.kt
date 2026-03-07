package com.rejowan.pdfreaderpro.util

import com.rejowan.pdfreaderpro.R

/**
 * Utility object for validating user inputs across the app.
 * Provides consistent validation rules for common input types.
 */
object InputValidation {

    // Password validation
    const val MIN_PASSWORD_LENGTH = 4
    const val MAX_PASSWORD_LENGTH = 128

    // File name validation
    private val INVALID_FILENAME_CHARS = charArrayOf('/', '\\', ':', '*', '?', '"', '<', '>', '|', '\u0000')
    const val MAX_FILENAME_LENGTH = 255

    // Page range validation
    private val PAGE_RANGE_PATTERN = Regex("""^(\d+(-\d+)?)(,\s*\d+(-\d+)?)*$""")

    /**
     * Result of input validation.
     */
    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data class Invalid(val errorMessageResId: Int, val formatArgs: Array<Any> = emptyArray()) : ValidationResult() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Invalid) return false
                return errorMessageResId == other.errorMessageResId && formatArgs.contentEquals(other.formatArgs)
            }
            override fun hashCode(): Int {
                return 31 * errorMessageResId + formatArgs.contentHashCode()
            }
        }
    }

    /**
     * Validates a password string.
     */
    fun validatePassword(password: String, isRequired: Boolean = true): ValidationResult {
        return when {
            password.isEmpty() && isRequired -> ValidationResult.Invalid(R.string.validation_password_required)
            password.isEmpty() -> ValidationResult.Valid
            password.length < MIN_PASSWORD_LENGTH -> ValidationResult.Invalid(
                R.string.validation_password_too_short,
                arrayOf(MIN_PASSWORD_LENGTH)
            )
            password.length > MAX_PASSWORD_LENGTH -> ValidationResult.Invalid(
                R.string.validation_password_too_long,
                arrayOf(MAX_PASSWORD_LENGTH)
            )
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates a filename string.
     */
    fun validateFileName(fileName: String, isRequired: Boolean = true): ValidationResult {
        val trimmed = fileName.trim()
        return when {
            trimmed.isEmpty() && isRequired -> ValidationResult.Invalid(R.string.validation_filename_required)
            trimmed.isEmpty() -> ValidationResult.Valid
            trimmed.length > MAX_FILENAME_LENGTH -> ValidationResult.Invalid(
                R.string.validation_filename_too_long,
                arrayOf(MAX_FILENAME_LENGTH)
            )
            trimmed.any { it in INVALID_FILENAME_CHARS } -> ValidationResult.Invalid(R.string.validation_filename_invalid_chars)
            trimmed.startsWith('.') -> ValidationResult.Invalid(R.string.validation_filename_starts_with_dot)
            trimmed.endsWith('.') -> ValidationResult.Invalid(R.string.validation_filename_ends_with_dot)
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates a page number input.
     */
    fun validatePageNumber(input: String, totalPages: Int): ValidationResult {
        if (input.isEmpty()) return ValidationResult.Invalid(R.string.validation_page_required)

        val pageNum = input.toIntOrNull()
            ?: return ValidationResult.Invalid(R.string.validation_page_invalid)

        return when {
            pageNum < 1 -> ValidationResult.Invalid(R.string.validation_page_too_low)
            pageNum > totalPages -> ValidationResult.Invalid(
                R.string.validation_page_too_high,
                arrayOf(totalPages)
            )
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates a page range string (e.g., "1-5, 7, 10-12").
     */
    fun validatePageRange(input: String, totalPages: Int): ValidationResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ValidationResult.Invalid(R.string.validation_page_range_required)

        // Check format
        if (!PAGE_RANGE_PATTERN.matches(trimmed)) {
            return ValidationResult.Invalid(R.string.validation_page_range_format)
        }

        // Parse and validate each part
        val parts = trimmed.split(",").map { it.trim() }
        for (part in parts) {
            if (part.contains("-")) {
                val range = part.split("-")
                val start = range[0].toIntOrNull() ?: return ValidationResult.Invalid(R.string.validation_page_range_format)
                val end = range[1].toIntOrNull() ?: return ValidationResult.Invalid(R.string.validation_page_range_format)

                if (start < 1 || end < 1) return ValidationResult.Invalid(R.string.validation_page_too_low)
                if (start > totalPages || end > totalPages) {
                    return ValidationResult.Invalid(R.string.validation_page_too_high, arrayOf(totalPages))
                }
                if (start > end) return ValidationResult.Invalid(R.string.validation_page_range_invalid)
            } else {
                val page = part.toIntOrNull() ?: return ValidationResult.Invalid(R.string.validation_page_range_format)
                if (page < 1) return ValidationResult.Invalid(R.string.validation_page_too_low)
                if (page > totalPages) {
                    return ValidationResult.Invalid(R.string.validation_page_too_high, arrayOf(totalPages))
                }
            }
        }

        return ValidationResult.Valid
    }

    /**
     * Validates a numeric input within a range.
     */
    fun validateNumericRange(input: String, min: Int, max: Int, fieldName: String = "Value"): ValidationResult {
        if (input.isEmpty()) return ValidationResult.Invalid(R.string.validation_value_required)

        val value = input.toIntOrNull()
            ?: return ValidationResult.Invalid(R.string.validation_value_invalid)

        return when {
            value < min -> ValidationResult.Invalid(R.string.validation_value_too_low, arrayOf(min))
            value > max -> ValidationResult.Invalid(R.string.validation_value_too_high, arrayOf(max))
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates hex color input (RGB values 0-255).
     */
    fun validateColorComponent(input: String): ValidationResult {
        if (input.isEmpty()) return ValidationResult.Valid // Empty is ok, will use default

        val value = input.toIntOrNull()
            ?: return ValidationResult.Invalid(R.string.validation_color_invalid)

        return when {
            value < 0 || value > 255 -> ValidationResult.Invalid(R.string.validation_color_range)
            else -> ValidationResult.Valid
        }
    }

    /**
     * Helper extension to check if validation passed.
     */
    fun ValidationResult.isValid(): Boolean = this is ValidationResult.Valid

    /**
     * Helper extension to check if validation failed.
     */
    fun ValidationResult.isInvalid(): Boolean = this is ValidationResult.Invalid
}
