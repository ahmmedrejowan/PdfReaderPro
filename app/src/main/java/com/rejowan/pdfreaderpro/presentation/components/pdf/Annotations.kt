package com.bhuvaneshw.pdf

import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.LOCAL_VARIABLE
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER
import kotlin.annotation.AnnotationTarget.TYPEALIAS
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

/**
 * Denotes that the annotated API is part of the PDF editor functionality, which may not be
 * available on all devices.
 *
 * This annotation is a marker for APIs that should be used with caution, as they might not
 * be present or work as expected on older devices or devices with limited capabilities.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Pdf Editor Api may not be available in all devices!"
)
@Retention(AnnotationRetention.BINARY)
@Target(
    CLASS,
    ANNOTATION_CLASS,
    PROPERTY,
    FIELD,
    LOCAL_VARIABLE,
    VALUE_PARAMETER,
    CONSTRUCTOR,
    FUNCTION,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    TYPEALIAS,
)
annotation class PdfEditorModeApi

/**
 * Marks APIs that are not yet stable and may change in the future.
 *
 * These APIs are experimental and should not be relied upon in production code.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This Api is not stable yet!"
)
@Retention(AnnotationRetention.BINARY)
@Target(
    CLASS,
    ANNOTATION_CLASS,
    PROPERTY,
    FIELD,
    LOCAL_VARIABLE,
    VALUE_PARAMETER,
    CONSTRUCTOR,
    FUNCTION,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    TYPEALIAS,
)
annotation class PdfUnstableApi

/**
 * Indicates that the annotated print-related API may not work correctly if the PDF has been
 * modified with highlights, text, or ink annotations.
 *
 * Use this with caution when printing documents that have been edited.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This won't work if the pdf is modified with Highlight, Text or Ink!"
)
@Retention(AnnotationRetention.BINARY)
@Target(
    CLASS,
    ANNOTATION_CLASS,
    PROPERTY,
    FIELD,
    LOCAL_VARIABLE,
    VALUE_PARAMETER,
    CONSTRUCTOR,
    FUNCTION,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    TYPEALIAS,
)
annotation class PdfUnstablePrintApi
