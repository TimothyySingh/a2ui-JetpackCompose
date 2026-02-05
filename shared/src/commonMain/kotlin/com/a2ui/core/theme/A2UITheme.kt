package com.a2ui.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Theme configuration for A2UI components.
 * Allows customization of colors, typography, spacing, and component-specific styles.
 */
data class A2UITheme(
    val colors: Colors = Colors(),
    val typography: Typography = Typography(),
    val spacing: Spacing = Spacing(),
    val shapes: Shapes = Shapes(),
    val components: ComponentStyles = ComponentStyles()
) {
    /**
     * Color palette
     */
    data class Colors(
        val primary: Color = Color(0xFF1976D2),
        val primaryVariant: Color = Color(0xFF004BA0),
        val secondary: Color = Color(0xFF03DAC6),
        val secondaryVariant: Color = Color(0xFF018786),
        val background: Color = Color.White,
        val surface: Color = Color.White,
        val error: Color = Color(0xFFB00020),
        val onPrimary: Color = Color.White,
        val onSecondary: Color = Color.Black,
        val onBackground: Color = Color.Black,
        val onSurface: Color = Color.Black,
        val onError: Color = Color.White
    )
    
    /**
     * Typography configuration
     */
    data class Typography(
        val h1: TextStyle = TextStyle(fontSize = 32.sp, lineHeight = 40.sp),
        val h2: TextStyle = TextStyle(fontSize = 28.sp, lineHeight = 36.sp),
        val h3: TextStyle = TextStyle(fontSize = 24.sp, lineHeight = 32.sp),
        val h4: TextStyle = TextStyle(fontSize = 20.sp, lineHeight = 28.sp),
        val h5: TextStyle = TextStyle(fontSize = 18.sp, lineHeight = 24.sp),
        val body: TextStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
        val caption: TextStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
        val button: TextStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
        val overline: TextStyle = TextStyle(fontSize = 12.sp, lineHeight = 16.sp)
    )
    
    /**
     * Spacing configuration
     */
    data class Spacing(
        val xs: Dp = 4.dp,
        val sm: Dp = 8.dp,
        val md: Dp = 16.dp,
        val lg: Dp = 24.dp,
        val xl: Dp = 32.dp,
        val xxl: Dp = 48.dp
    )
    
    /**
     * Shape configuration
     */
    data class Shapes(
        val small: Dp = 4.dp,
        val medium: Dp = 8.dp,
        val large: Dp = 16.dp
    )
    
    /**
     * Component-specific style overrides
     */
    data class ComponentStyles(
        val button: ButtonStyles = ButtonStyles(),
        val card: CardStyles = CardStyles(),
        val textField: TextFieldStyles = TextFieldStyles()
    )
    
    /**
     * Button style configuration
     */
    data class ButtonStyles(
        val horizontalPadding: Dp = 16.dp,
        val verticalPadding: Dp = 8.dp,
        val minHeight: Dp = 48.dp,
        val cornerRadius: Dp = 8.dp
    )
    
    /**
     * Card style configuration
     */
    data class CardStyles(
        val elevation: Dp = 4.dp,
        val padding: Dp = 16.dp,
        val cornerRadius: Dp = 8.dp
    )
    
    /**
     * TextField style configuration
     */
    data class TextFieldStyles(
        val padding: Dp = 12.dp,
        val cornerRadius: Dp = 4.dp
    )
    
    /**
     * Text style configuration
     */
    data class TextStyle(
        val fontSize: TextUnit,
        val lineHeight: TextUnit,
        val letterSpacing: TextUnit = 0.sp,
        val fontWeight: androidx.compose.ui.text.font.FontWeight = androidx.compose.ui.text.font.FontWeight.Normal
    )
    
    companion object {
        /**
         * Default theme
         */
        val Default = A2UITheme()
        
        /**
         * Dark theme preset
         */
        val Dark = A2UITheme(
            colors = Colors(
                primary = Color(0xFF90CAF9),
                primaryVariant = Color(0xFF42A5F5),
                secondary = Color(0xFF03DAC6),
                secondaryVariant = Color(0xFF03DAC6),
                background = Color(0xFF121212),
                surface = Color(0xFF1E1E1E),
                error = Color(0xFFCF6679),
                onPrimary = Color.Black,
                onSecondary = Color.Black,
                onBackground = Color.White,
                onSurface = Color.White,
                onError = Color.Black
            )
        )
        
        /**
         * High contrast theme
         */
        val HighContrast = A2UITheme(
            colors = Colors(
                primary = Color.Black,
                primaryVariant = Color(0xFF333333),
                secondary = Color(0xFF666666),
                secondaryVariant = Color(0xFF999999),
                background = Color.White,
                surface = Color.White,
                error = Color.Red,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color.Black,
                onSurface = Color.Black,
                onError = Color.White
            )
        )
    }
}

/**
 * Builder for creating custom themes
 */
class A2UIThemeBuilder {
    private var colors = A2UITheme.Colors()
    private var typography = A2UITheme.Typography()
    private var spacing = A2UITheme.Spacing()
    private var shapes = A2UITheme.Shapes()
    private var components = A2UITheme.ComponentStyles()
    
    fun colors(block: A2UITheme.Colors.() -> A2UITheme.Colors) {
        colors = colors.block()
    }
    
    fun typography(block: A2UITheme.Typography.() -> A2UITheme.Typography) {
        typography = typography.block()
    }
    
    fun spacing(block: A2UITheme.Spacing.() -> A2UITheme.Spacing) {
        spacing = spacing.block()
    }
    
    fun shapes(block: A2UITheme.Shapes.() -> A2UITheme.Shapes) {
        shapes = shapes.block()
    }
    
    fun components(block: A2UITheme.ComponentStyles.() -> A2UITheme.ComponentStyles) {
        components = components.block()
    }
    
    fun build(): A2UITheme = A2UITheme(colors, typography, spacing, shapes, components)
}

/**
 * DSL for building custom themes
 */
fun buildA2UITheme(block: A2UIThemeBuilder.() -> Unit): A2UITheme {
    return A2UIThemeBuilder().apply(block).build()
}