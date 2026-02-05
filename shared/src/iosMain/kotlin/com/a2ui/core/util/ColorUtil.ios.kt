package com.a2ui.core.util

import androidx.compose.ui.graphics.Color

actual fun parseColor(colorString: String): Color {
    val hex = if (colorString.startsWith("#")) colorString.substring(1) else colorString
    
    return when (hex.length) {
        6 -> {
            val r = hex.substring(0, 2).toInt(16)
            val g = hex.substring(2, 4).toInt(16)
            val b = hex.substring(4, 6).toInt(16)
            Color(r, g, b)
        }
        8 -> {
            val a = hex.substring(0, 2).toInt(16)
            val r = hex.substring(2, 4).toInt(16)
            val g = hex.substring(4, 6).toInt(16)
            val b = hex.substring(6, 8).toInt(16)
            Color(r, g, b, a)
        }
        else -> Color.Unspecified
    }
}
