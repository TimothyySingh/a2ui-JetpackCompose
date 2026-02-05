package com.a2ui.examples

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test suite for the custom component examples - v0.9
 *
 * Verifies createCustomComponentRegistry() and createCustomTheme()
 * using PascalCase type strings and the new v0.9 model types.
 */
class CustomComponentExampleTest {

    // ------------------------------------------------------------------
    // Custom component registry
    // ------------------------------------------------------------------

    @Test
    fun testCustomComponentRegistryHasButton() {
        val registry = createCustomComponentRegistry()
        assertTrue(registry.has("Button"))
    }

    @Test
    fun testCustomComponentRegistryHasCard() {
        val registry = createCustomComponentRegistry()
        assertTrue(registry.has("Card"))
    }

    @Test
    fun testCustomComponentRegistryHasChip() {
        val registry = createCustomComponentRegistry()
        assertTrue(registry.has("Chip"))
    }

    @Test
    fun testCustomComponentRegistryHasAnimatedText() {
        val registry = createCustomComponentRegistry()
        assertTrue(registry.has("AnimatedText"))
    }

    @Test
    fun testCustomComponentRegistryCreation() {
        val registry = createCustomComponentRegistry()

        // All four PascalCase types should be registered
        assertTrue(registry.has("Button"))
        assertTrue(registry.has("Card"))
        assertTrue(registry.has("Chip"))
        assertTrue(registry.has("AnimatedText"))
    }

    @Test
    fun testCustomComponentRegistryDoesNotHaveUnregisteredTypes() {
        val registry = createCustomComponentRegistry()

        // These types were NOT registered by createCustomComponentRegistry()
        assertTrue(!registry.has("Text"))
        assertTrue(!registry.has("TextField"))
        assertTrue(!registry.has("Row"))
        assertTrue(!registry.has("Column"))
    }

    // ------------------------------------------------------------------
    // Custom theme - colors
    // ------------------------------------------------------------------

    @Test
    fun testCustomThemeCreation() {
        val customTheme = createCustomTheme()

        assertEquals(Color(0xFF667EEA), customTheme.colors.primary)
        assertEquals(Color(0xFF764BA2), customTheme.colors.primaryVariant)
        assertEquals(Color(0xFFFFC107), customTheme.colors.secondary)
        assertEquals(Color(0xFFF7F8FA), customTheme.colors.background)
        assertEquals(Color.White, customTheme.colors.surface)
    }

    @Test
    fun testCustomThemePrimaryColor() {
        val customTheme = createCustomTheme()
        assertEquals(Color(0xFF667EEA), customTheme.colors.primary)
    }

    @Test
    fun testCustomThemeSecondaryColor() {
        val customTheme = createCustomTheme()
        assertEquals(Color(0xFFFFC107), customTheme.colors.secondary)
    }

    // ------------------------------------------------------------------
    // Custom theme - typography
    // ------------------------------------------------------------------

    @Test
    fun testCustomThemeTypography() {
        val customTheme = createCustomTheme()

        assertEquals(36.sp, customTheme.typography.h1.fontSize)
        assertEquals(32.sp, customTheme.typography.h2.fontSize)
        assertEquals(16.sp, customTheme.typography.body.fontSize)
        assertEquals(24.sp, customTheme.typography.body.lineHeight)
    }

    // ------------------------------------------------------------------
    // Custom theme - spacing
    // ------------------------------------------------------------------

    @Test
    fun testCustomThemeSpacing() {
        val customTheme = createCustomTheme()

        assertEquals(4.dp, customTheme.spacing.xs)
        assertEquals(8.dp, customTheme.spacing.sm)
        assertEquals(16.dp, customTheme.spacing.md)
        assertEquals(32.dp, customTheme.spacing.lg)
        assertEquals(48.dp, customTheme.spacing.xl)
    }

    // ------------------------------------------------------------------
    // Custom theme - component styles
    // ------------------------------------------------------------------

    @Test
    fun testCustomThemeButtonStyles() {
        val customTheme = createCustomTheme()

        assertEquals(56.dp, customTheme.components.button.minHeight)
        assertEquals(24.dp, customTheme.components.button.horizontalPadding)
        assertEquals(12.dp, customTheme.components.button.cornerRadius)
    }

    @Test
    fun testCustomThemeCardStyles() {
        val customTheme = createCustomTheme()

        assertEquals(8.dp, customTheme.components.card.elevation)
        assertEquals(20.dp, customTheme.components.card.padding)
        assertEquals(20.dp, customTheme.components.card.cornerRadius)
    }

    @Test
    fun testCustomThemeComponents() {
        val customTheme = createCustomTheme()

        // Button
        assertEquals(56.dp, customTheme.components.button.minHeight)
        assertEquals(12.dp, customTheme.components.button.cornerRadius)

        // Card
        assertEquals(20.dp, customTheme.components.card.cornerRadius)
        assertEquals(8.dp, customTheme.components.card.elevation)
    }
}
