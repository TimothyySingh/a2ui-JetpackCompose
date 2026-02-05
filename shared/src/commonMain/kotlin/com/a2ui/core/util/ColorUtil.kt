package com.a2ui.core.util

import androidx.compose.ui.graphics.Color

/**
 * Platform-agnostic color parsing
 */
expect fun parseColor(colorString: String): Color

/**
 * Parse color with fallback
 */
fun parseColorOrDefault(colorString: String?, default: Color = Color.Unspecified): Color {
    if (colorString == null) return default
    return try {
        parseColor(colorString)
    } catch (e: Exception) {
        default
    }
}
