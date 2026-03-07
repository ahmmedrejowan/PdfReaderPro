package com.rejowan.pdfreaderpro.util

import com.rejowan.pdfreaderpro.util.InputValidation.ValidationResult
import com.rejowan.pdfreaderpro.util.InputValidation.isInvalid
import com.rejowan.pdfreaderpro.util.InputValidation.isValid
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for InputValidation utility.
 */
class InputValidationTest {

    // region Password Validation Tests
    @Test
    fun `validatePassword returns valid for password meeting requirements`() {
        val result = InputValidation.validatePassword("password123")
        assertTrue(result.isValid())
    }

    @Test
    fun `validatePassword returns valid for minimum length password`() {
        val result = InputValidation.validatePassword("1234")
        assertTrue(result.isValid())
    }

    @Test
    fun `validatePassword returns invalid for empty required password`() {
        val result = InputValidation.validatePassword("", isRequired = true)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validatePassword returns valid for empty optional password`() {
        val result = InputValidation.validatePassword("", isRequired = false)
        assertTrue(result.isValid())
    }

    @Test
    fun `validatePassword returns invalid for too short password`() {
        val result = InputValidation.validatePassword("123")
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validatePassword returns invalid for password exceeding max length`() {
        val longPassword = "a".repeat(129)
        val result = InputValidation.validatePassword(longPassword)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validatePassword returns valid for max length password`() {
        val maxPassword = "a".repeat(128)
        val result = InputValidation.validatePassword(maxPassword)
        assertTrue(result.isValid())
    }

    @Test
    fun `validatePassword handles special characters`() {
        val result = InputValidation.validatePassword("p@ss!w0rd#$%")
        assertTrue(result.isValid())
    }

    @Test
    fun `validatePassword handles unicode characters`() {
        val result = InputValidation.validatePassword("密码1234")
        assertTrue(result.isValid())
    }
    // endregion

    // region File Name Validation Tests
    @Test
    fun `validateFileName returns valid for normal filename`() {
        val result = InputValidation.validateFileName("my_document")
        assertTrue(result.isValid())
    }

    @Test
    fun `validateFileName returns invalid for empty required filename`() {
        val result = InputValidation.validateFileName("", isRequired = true)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateFileName returns valid for empty optional filename`() {
        val result = InputValidation.validateFileName("", isRequired = false)
        assertTrue(result.isValid())
    }

    @Test
    fun `validateFileName returns invalid for filename with slash`() {
        val result = InputValidation.validateFileName("path/file")
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateFileName returns invalid for filename with backslash`() {
        val result = InputValidation.validateFileName("path\\file")
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateFileName returns invalid for filename with colon`() {
        val result = InputValidation.validateFileName("file:name")
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateFileName returns invalid for filename with asterisk`() {
        val result = InputValidation.validateFileName("file*name")
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateFileName returns invalid for filename with question mark`() {
        val result = InputValidation.validateFileName("file?name")
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateFileName returns invalid for filename with quotes`() {
        val result = InputValidation.validateFileName("file\"name")
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateFileName returns invalid for filename with angle brackets`() {
        val result1 = InputValidation.validateFileName("file<name")
        val result2 = InputValidation.validateFileName("file>name")
        assertTrue(result1.isInvalid())
        assertTrue(result2.isInvalid())
    }

    @Test
    fun `validateFileName returns invalid for filename with pipe`() {
        val result = InputValidation.validateFileName("file|name")
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateFileName returns invalid for filename starting with dot`() {
        val result = InputValidation.validateFileName(".hidden")
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateFileName returns invalid for filename ending with dot`() {
        val result = InputValidation.validateFileName("filename.")
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateFileName returns valid for filename with dot in middle`() {
        val result = InputValidation.validateFileName("file.name")
        assertTrue(result.isValid())
    }

    @Test
    fun `validateFileName trims whitespace`() {
        val result = InputValidation.validateFileName("  filename  ")
        assertTrue(result.isValid())
    }

    @Test
    fun `validateFileName returns invalid for whitespace only`() {
        val result = InputValidation.validateFileName("   ", isRequired = true)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateFileName returns invalid for too long filename`() {
        val longName = "a".repeat(256)
        val result = InputValidation.validateFileName(longName)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateFileName returns valid for max length filename`() {
        val maxName = "a".repeat(255)
        val result = InputValidation.validateFileName(maxName)
        assertTrue(result.isValid())
    }

    @Test
    fun `validateFileName handles unicode characters`() {
        val result = InputValidation.validateFileName("文档_документ_αρχείο")
        assertTrue(result.isValid())
    }

    @Test
    fun `validateFileName handles spaces in name`() {
        val result = InputValidation.validateFileName("my document name")
        assertTrue(result.isValid())
    }

    @Test
    fun `validateFileName handles hyphens and underscores`() {
        val result = InputValidation.validateFileName("my-document_name-v2")
        assertTrue(result.isValid())
    }
    // endregion

    // region Page Number Validation Tests
    @Test
    fun `validatePageNumber returns valid for page within range`() {
        val result = InputValidation.validatePageNumber("5", totalPages = 10)
        assertTrue(result.isValid())
    }

    @Test
    fun `validatePageNumber returns valid for first page`() {
        val result = InputValidation.validatePageNumber("1", totalPages = 10)
        assertTrue(result.isValid())
    }

    @Test
    fun `validatePageNumber returns valid for last page`() {
        val result = InputValidation.validatePageNumber("10", totalPages = 10)
        assertTrue(result.isValid())
    }

    @Test
    fun `validatePageNumber returns invalid for empty input`() {
        val result = InputValidation.validatePageNumber("", totalPages = 10)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validatePageNumber returns invalid for non-numeric input`() {
        val result = InputValidation.validatePageNumber("abc", totalPages = 10)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validatePageNumber returns invalid for zero`() {
        val result = InputValidation.validatePageNumber("0", totalPages = 10)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validatePageNumber returns invalid for negative number`() {
        val result = InputValidation.validatePageNumber("-1", totalPages = 10)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validatePageNumber returns invalid for page exceeding total`() {
        val result = InputValidation.validatePageNumber("11", totalPages = 10)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validatePageNumber handles single page document`() {
        val result = InputValidation.validatePageNumber("1", totalPages = 1)
        assertTrue(result.isValid())

        val result2 = InputValidation.validatePageNumber("2", totalPages = 1)
        assertTrue(result2.isInvalid())
    }
    // endregion

    // region Page Range Validation Tests
    @Test
    fun `validatePageRange returns valid for simple range`() {
        val result = InputValidation.validatePageRange("1-5", totalPages = 10)
        assertTrue(result.isValid())
    }

    @Test
    fun `validatePageRange returns valid for single page`() {
        val result = InputValidation.validatePageRange("5", totalPages = 10)
        assertTrue(result.isValid())
    }

    @Test
    fun `validatePageRange returns valid for comma separated pages`() {
        val result = InputValidation.validatePageRange("1, 3, 5", totalPages = 10)
        assertTrue(result.isValid())
    }

    @Test
    fun `validatePageRange returns valid for mixed ranges and pages`() {
        val result = InputValidation.validatePageRange("1-3, 5, 7-9", totalPages = 10)
        assertTrue(result.isValid())
    }

    @Test
    fun `validatePageRange returns invalid for empty input`() {
        val result = InputValidation.validatePageRange("", totalPages = 10)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validatePageRange returns invalid for invalid format`() {
        val result = InputValidation.validatePageRange("1-2-3", totalPages = 10)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validatePageRange returns invalid when start exceeds end`() {
        val result = InputValidation.validatePageRange("5-3", totalPages = 10)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validatePageRange returns invalid for page exceeding total`() {
        val result = InputValidation.validatePageRange("1-15", totalPages = 10)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validatePageRange returns invalid for zero page`() {
        val result = InputValidation.validatePageRange("0-5", totalPages = 10)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validatePageRange trims whitespace`() {
        val result = InputValidation.validatePageRange("  1-5  ", totalPages = 10)
        assertTrue(result.isValid())
    }
    // endregion

    // region Numeric Range Validation Tests
    @Test
    fun `validateNumericRange returns valid for value in range`() {
        val result = InputValidation.validateNumericRange("50", min = 0, max = 100)
        assertTrue(result.isValid())
    }

    @Test
    fun `validateNumericRange returns valid for min value`() {
        val result = InputValidation.validateNumericRange("0", min = 0, max = 100)
        assertTrue(result.isValid())
    }

    @Test
    fun `validateNumericRange returns valid for max value`() {
        val result = InputValidation.validateNumericRange("100", min = 0, max = 100)
        assertTrue(result.isValid())
    }

    @Test
    fun `validateNumericRange returns invalid for value below min`() {
        val result = InputValidation.validateNumericRange("-1", min = 0, max = 100)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateNumericRange returns invalid for value above max`() {
        val result = InputValidation.validateNumericRange("101", min = 0, max = 100)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateNumericRange returns invalid for empty input`() {
        val result = InputValidation.validateNumericRange("", min = 0, max = 100)
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateNumericRange returns invalid for non-numeric input`() {
        val result = InputValidation.validateNumericRange("abc", min = 0, max = 100)
        assertTrue(result.isInvalid())
    }
    // endregion

    // region Color Component Validation Tests
    @Test
    fun `validateColorComponent returns valid for value in range`() {
        val result = InputValidation.validateColorComponent("128")
        assertTrue(result.isValid())
    }

    @Test
    fun `validateColorComponent returns valid for zero`() {
        val result = InputValidation.validateColorComponent("0")
        assertTrue(result.isValid())
    }

    @Test
    fun `validateColorComponent returns valid for 255`() {
        val result = InputValidation.validateColorComponent("255")
        assertTrue(result.isValid())
    }

    @Test
    fun `validateColorComponent returns valid for empty input`() {
        val result = InputValidation.validateColorComponent("")
        assertTrue(result.isValid())
    }

    @Test
    fun `validateColorComponent returns invalid for negative value`() {
        val result = InputValidation.validateColorComponent("-1")
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateColorComponent returns invalid for value over 255`() {
        val result = InputValidation.validateColorComponent("256")
        assertTrue(result.isInvalid())
    }

    @Test
    fun `validateColorComponent returns invalid for non-numeric input`() {
        val result = InputValidation.validateColorComponent("FF")
        assertTrue(result.isInvalid())
    }
    // endregion

    // region Edge Cases
    @Test
    fun `validation results have correct equals and hashCode`() {
        val invalid1 = ValidationResult.Invalid(1, arrayOf("test"))
        val invalid2 = ValidationResult.Invalid(1, arrayOf("test"))
        val invalid3 = ValidationResult.Invalid(2, arrayOf("test"))

        assertEquals(invalid1, invalid2)
        assertEquals(invalid1.hashCode(), invalid2.hashCode())
        assertNotEquals(invalid1, invalid3)
    }

    @Test
    fun `isValid extension works correctly`() {
        assertTrue(ValidationResult.Valid.isValid())
        assertFalse(ValidationResult.Invalid(1).isValid())
    }

    @Test
    fun `isInvalid extension works correctly`() {
        assertFalse(ValidationResult.Valid.isInvalid())
        assertTrue(ValidationResult.Invalid(1).isInvalid())
    }
    // endregion
}
