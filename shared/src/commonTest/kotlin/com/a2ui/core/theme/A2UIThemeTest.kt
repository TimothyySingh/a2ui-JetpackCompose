package com.a2ui.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a2ui.core.model.A2UISurfaceTheme
import kotlin.test.*

/**
 * Test suite for A2UITheme - v0.9
 *
 * Covers Default / Dark / HighContrast presets, the builder DSL,
 * all data-class copy operations, and the new withSurfaceTheme extension.
 */
class A2UIThemeTest {

    // ------------------------------------------------------------------
    // Default theme
    // ------------------------------------------------------------------

    @Test
    fun testDefaultTheme() {
        val theme = A2UITheme.Default

        assertEquals(Color(0xFF1976D2), theme.colors.primary)
        assertEquals(Color.White, theme.colors.background)
        assertEquals(16.sp, theme.typography.body.fontSize)
        assertEquals(16.dp, theme.spacing.md)
        assertEquals(8.dp, theme.shapes.medium)
    }

    // ------------------------------------------------------------------
    // Dark theme
    // ------------------------------------------------------------------

    @Test
    fun testDarkTheme() {
        val theme = A2UITheme.Dark

        assertEquals(Color(0xFF90CAF9), theme.colors.primary)
        assertEquals(Color(0xFF121212), theme.colors.background)
        assertEquals(Color.White, theme.colors.onBackground)
        assertEquals(Color.Black, theme.colors.onPrimary)
    }

    // ------------------------------------------------------------------
    // High contrast theme
    // ------------------------------------------------------------------

    @Test
    fun testHighContrastTheme() {
        val theme = A2UITheme.HighContrast

        assertEquals(Color.Black, theme.colors.primary)
        assertEquals(Color.White, theme.colors.background)
        assertEquals(Color.Red, theme.colors.error)
        assertEquals(Color.White, theme.colors.onPrimary)
        assertEquals(Color.Black, theme.colors.onBackground)
    }

    // ------------------------------------------------------------------
    // withSurfaceTheme
    // ------------------------------------------------------------------

    @Test
    fun testSurfaceThemePrimaryColor() {
        val surfaceTheme = A2UISurfaceTheme(primaryColor = "#FF0000")
        val themed = A2UITheme.Default.withSurfaceTheme(surfaceTheme)

        // Primary color should be overridden to red
        assertEquals(Color(0xFFFF0000.toInt()), themed.colors.primary)

        // Everything else should remain at Default values
        assertEquals(A2UITheme.Default.colors.background, themed.colors.background)
        assertEquals(A2UITheme.Default.colors.secondary, themed.colors.secondary)
        assertEquals(A2UITheme.Default.typography, themed.typography)
        assertEquals(A2UITheme.Default.spacing, themed.spacing)
    }

    @Test
    fun testSurfaceThemeNullPrimaryColorReturnsOriginal() {
        val surfaceTheme = A2UISurfaceTheme(primaryColor = null)
        val themed = A2UITheme.Default.withSurfaceTheme(surfaceTheme)

        assertEquals(A2UITheme.Default.colors.primary, themed.colors.primary)
    }

    @Test
    fun testSurfaceThemeNullReturnsOriginal() {
        val themed = A2UITheme.Default.withSurfaceTheme(null)
        assertEquals(A2UITheme.Default, themed)
    }

    // ------------------------------------------------------------------
    // Theme builder DSL
    // ------------------------------------------------------------------

    @Test
    fun testThemeBuilder() {
        val theme = buildA2UITheme {
            colors {
                copy(
                    primary = Color.Blue,
                    secondary = Color.Green,
                    background = Color.Gray
                )
            }
            typography {
                copy(
                    h1 = h1.copy(fontSize = 40.sp),
                    body = body.copy(fontSize = 18.sp)
                )
            }
            spacing {
                copy(
                    sm = 4.dp,
                    md = 12.dp,
                    lg = 20.dp
                )
            }
            shapes {
                copy(
                    small = 2.dp,
                    medium = 6.dp,
                    large = 12.dp
                )
            }
            components {
                copy(
                    button = button.copy(
                        minHeight = 60.dp,
                        cornerRadius = 16.dp
                    ),
                    card = card.copy(
                        elevation = 12.dp,
                        padding = 24.dp
                    )
                )
            }
        }

        assertEquals(Color.Blue, theme.colors.primary)
        assertEquals(Color.Green, theme.colors.secondary)
        assertEquals(Color.Gray, theme.colors.background)
        assertEquals(40.sp, theme.typography.h1.fontSize)
        assertEquals(18.sp, theme.typography.body.fontSize)
        assertEquals(4.dp, theme.spacing.sm)
        assertEquals(12.dp, theme.spacing.md)
        assertEquals(20.dp, theme.spacing.lg)
        assertEquals(2.dp, theme.shapes.small)
        assertEquals(6.dp, theme.shapes.medium)
        assertEquals(12.dp, theme.shapes.large)
        assertEquals(60.dp, theme.components.button.minHeight)
        assertEquals(16.dp, theme.components.button.cornerRadius)
        assertEquals(12.dp, theme.components.card.elevation)
        assertEquals(24.dp, theme.components.card.padding)
    }

    // ------------------------------------------------------------------
    // Data class copy tests
    // ------------------------------------------------------------------

    @Test
    fun testColorsDataClass() {
        val colors = A2UITheme.Colors()

        val modified = colors.copy(
            primary = Color.Red,
            onPrimary = Color.Yellow
        )

        assertEquals(Color.Red, modified.primary)
        assertEquals(Color.Yellow, modified.onPrimary)
        assertEquals(colors.secondary, modified.secondary) // Others remain unchanged
    }

    @Test
    fun testTypographyDataClass() {
        val typography = A2UITheme.Typography()

        val modified = typography.copy(
            h1 = A2UITheme.TextStyle(
                fontSize = 48.sp,
                lineHeight = 56.sp,
                fontWeight = FontWeight.Black
            )
        )

        assertEquals(48.sp, modified.h1.fontSize)
        assertEquals(56.sp, modified.h1.lineHeight)
        assertEquals(FontWeight.Black, modified.h1.fontWeight)
        assertEquals(typography.body, modified.body) // Others unchanged
    }

    @Test
    fun testSpacingDataClass() {
        val spacing = A2UITheme.Spacing()

        val modified = spacing.copy(
            xs = 2.dp,
            xxl = 64.dp
        )

        assertEquals(2.dp, modified.xs)
        assertEquals(64.dp, modified.xxl)
        assertEquals(spacing.md, modified.md) // Others unchanged
    }

    @Test
    fun testButtonStylesDataClass() {
        val buttonStyles = A2UITheme.ButtonStyles()

        val modified = buttonStyles.copy(
            minHeight = 64.dp,
            horizontalPadding = 32.dp,
            verticalPadding = 16.dp,
            cornerRadius = 24.dp
        )

        assertEquals(64.dp, modified.minHeight)
        assertEquals(32.dp, modified.horizontalPadding)
        assertEquals(16.dp, modified.verticalPadding)
        assertEquals(24.dp, modified.cornerRadius)
    }

    @Test
    fun testCardStylesDataClass() {
        val cardStyles = A2UITheme.CardStyles()

        val modified = cardStyles.copy(
            elevation = 16.dp,
            padding = 32.dp,
            cornerRadius = 24.dp
        )

        assertEquals(16.dp, modified.elevation)
        assertEquals(32.dp, modified.padding)
        assertEquals(24.dp, modified.cornerRadius)
    }

    @Test
    fun testTextFieldStylesDataClass() {
        val textFieldStyles = A2UITheme.TextFieldStyles()

        val modified = textFieldStyles.copy(
            padding = 16.dp,
            cornerRadius = 8.dp
        )

        assertEquals(16.dp, modified.padding)
        assertEquals(8.dp, modified.cornerRadius)
    }

    @Test
    fun testTextStyleDataClass() {
        val textStyle = A2UITheme.TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        val modified = textStyle.copy(
            fontSize = 20.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )

        assertEquals(20.sp, modified.fontSize)
        assertEquals(24.sp, modified.lineHeight) // Unchanged
        assertEquals(1.sp, modified.letterSpacing)
        assertEquals(FontWeight.Bold, modified.fontWeight)
    }

    // ------------------------------------------------------------------
    // Complete theme customization
    // ------------------------------------------------------------------

    @Test
    fun testCompleteThemeCustomization() {
        val customTheme = A2UITheme(
            colors = A2UITheme.Colors(
                primary = Color(0xFF123456),
                primaryVariant = Color(0xFF234567),
                secondary = Color(0xFF345678),
                secondaryVariant = Color(0xFF456789),
                background = Color(0xFF567890),
                surface = Color(0xFF6789AB),
                error = Color(0xFF789ABC),
                onPrimary = Color(0xFF89ABCD),
                onSecondary = Color(0xFF9ABCDE),
                onBackground = Color(0xFFABCDEF),
                onSurface = Color(0xFFBCDEF0),
                onError = Color(0xFFCDEF01)
            ),
            typography = A2UITheme.Typography(
                h1 = A2UITheme.TextStyle(fontSize = 50.sp, lineHeight = 60.sp),
                h2 = A2UITheme.TextStyle(fontSize = 45.sp, lineHeight = 55.sp),
                h3 = A2UITheme.TextStyle(fontSize = 40.sp, lineHeight = 50.sp),
                h4 = A2UITheme.TextStyle(fontSize = 35.sp, lineHeight = 45.sp),
                h5 = A2UITheme.TextStyle(fontSize = 30.sp, lineHeight = 40.sp),
                body = A2UITheme.TextStyle(fontSize = 20.sp, lineHeight = 30.sp),
                caption = A2UITheme.TextStyle(fontSize = 15.sp, lineHeight = 25.sp),
                button = A2UITheme.TextStyle(fontSize = 22.sp, lineHeight = 32.sp),
                overline = A2UITheme.TextStyle(fontSize = 10.sp, lineHeight = 15.sp)
            ),
            spacing = A2UITheme.Spacing(
                xs = 2.dp,
                sm = 6.dp,
                md = 14.dp,
                lg = 22.dp,
                xl = 30.dp,
                xxl = 46.dp
            ),
            shapes = A2UITheme.Shapes(
                small = 6.dp,
                medium = 12.dp,
                large = 24.dp
            ),
            components = A2UITheme.ComponentStyles(
                button = A2UITheme.ButtonStyles(
                    horizontalPadding = 20.dp,
                    verticalPadding = 10.dp,
                    minHeight = 50.dp,
                    cornerRadius = 10.dp
                ),
                card = A2UITheme.CardStyles(
                    elevation = 6.dp,
                    padding = 18.dp,
                    cornerRadius = 10.dp
                ),
                textField = A2UITheme.TextFieldStyles(
                    padding = 14.dp,
                    cornerRadius = 6.dp
                )
            )
        )

        assertEquals(Color(0xFF123456), customTheme.colors.primary)
        assertEquals(50.sp, customTheme.typography.h1.fontSize)
        assertEquals(2.dp, customTheme.spacing.xs)
        assertEquals(6.dp, customTheme.shapes.small)
        assertEquals(50.dp, customTheme.components.button.minHeight)
        assertEquals(6.dp, customTheme.components.card.elevation)
        assertEquals(14.dp, customTheme.components.textField.padding)
    }
}
