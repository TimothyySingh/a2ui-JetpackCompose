package com.a2ui.core.util

import androidx.compose.ui.graphics.Color

actual fun parseColor(colorString: String): Color {
    val hex = if (colorString.startsWith("#")) colorString else "#$colorString"
    return Color(android.graphics.Color.parseColor(hex))
}
