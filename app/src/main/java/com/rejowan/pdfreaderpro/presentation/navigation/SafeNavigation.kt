package com.rejowan.pdfreaderpro.presentation.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController

/**
 * Pops the current screen, but only while it is fully shown.
 *
 * A screen stays on screen and clickable during its exit animation, so a quick
 * second tap on its back button used to pop the screen below it as well. With
 * Home gone the NavHost had nothing to draw and the app showed a blank white
 * screen. During the animation the screen is no longer RESUMED, so the second
 * call does nothing. The last screen is never popped.
 */
fun NavController.popBackStackSafely(): Boolean {
    val entry = currentBackStackEntry ?: return false
    if (entry.lifecycle.currentState != Lifecycle.State.RESUMED) return false
    if (previousBackStackEntry == null) return false
    return popBackStack()
}
