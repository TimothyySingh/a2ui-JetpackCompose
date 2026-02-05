package com.a2ui.core.provider

import com.a2ui.core.model.A2UIComponent
import com.a2ui.core.model.A2UISurface
import com.a2ui.core.registry.ComponentRegistry
import com.a2ui.core.theme.A2UITheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test suite for A2UIProvider - v0.9
 *
 * These are conceptual tests (no ComposeTestRule). They verify the
 * A2UIConfig data class, default values, and ComponentRegistry basic
 * operations in a provider-like context using A2UISurface types.
 */
class A2UIProviderTest {

    // ------------------------------------------------------------------
    // A2UIConfig data class
    // ------------------------------------------------------------------

    @Test
    fun testConfigDataClassDefaults() {
        val config = A2UIConfig()

        assertNotNull(config.componentRegistry)
        assertEquals(A2UITheme.Default, config.theme)
        assertFalse(config.debug)
    }

    @Test
    fun testConfigDataClassCustomValues() {
        val customRegistry = ComponentRegistry().apply {
            register("Button") { _, _, _, _, _ -> }
        }
        val customTheme = A2UITheme.Dark

        val config = A2UIConfig(
            componentRegistry = customRegistry,
            theme = customTheme,
            debug = true
        )

        assertEquals(customRegistry, config.componentRegistry)
        assertEquals(customTheme, config.theme)
        assertTrue(config.debug)
    }

    @Test
    fun testConfigDataClassCopy() {
        val config = A2UIConfig(debug = false)
        val modified = config.copy(debug = true)

        assertFalse(config.debug)
        assertTrue(modified.debug)
    }

    // ------------------------------------------------------------------
    // Default values
    // ------------------------------------------------------------------

    @Test
    fun testDefaultConfigHasEmptyRegistry() {
        val config = A2UIConfig()
        assertFalse(config.componentRegistry.has("Button"))
        assertFalse(config.componentRegistry.has("Text"))
    }

    @Test
    fun testDefaultConfigUsesDefaultTheme() {
        val config = A2UIConfig()
        assertEquals(A2UITheme.Default, config.theme)
    }

    @Test
    fun testDefaultConfigDebugIsFalse() {
        val config = A2UIConfig()
        assertFalse(config.debug)
    }

    // ------------------------------------------------------------------
    // ComponentRegistry basic operations in provider context
    // ------------------------------------------------------------------

    @Test
    fun testProviderRegistryCanRegisterPascalCaseTypes() {
        val config = A2UIConfig(
            componentRegistry = ComponentRegistry().apply {
                register("Button") { _, _, _, _, _ -> }
                register("TextField") { _, _, _, _, _ -> }
                register("Card") { _, _, _, _, _ -> }
            }
        )

        assertTrue(config.componentRegistry.has("Button"))
        assertTrue(config.componentRegistry.has("TextField"))
        assertTrue(config.componentRegistry.has("Card"))
    }

    @Test
    fun testProviderRegistryWithA2UISurface() {
        // Build a simple A2UISurface and verify the config can reference it
        val surface = A2UISurface(
            root = "root",
            components = mapOf(
                "root" to A2UIComponent(
                    id = "root",
                    component = "Text"
                )
            )
        )

        val config = A2UIConfig(
            componentRegistry = ComponentRegistry().apply {
                register("Text") { _, _, _, _, _ -> }
            }
        )

        // The registry should have a renderer for the surface's root component type
        val rootComponent = surface.components[surface.root]
        assertNotNull(rootComponent)
        assertTrue(config.componentRegistry.has(rootComponent.component))
    }

    @Test
    fun testProviderWithHighContrastTheme() {
        // Conceptual: the provider should make the theme available
        val config = A2UIConfig(theme = A2UITheme.HighContrast)
        assertEquals(A2UITheme.HighContrast, config.theme)
    }

    @Test
    fun testProviderWithDarkTheme() {
        val config = A2UIConfig(theme = A2UITheme.Dark)
        assertEquals(A2UITheme.Dark, config.theme)
    }

    @Test
    fun testNestedProvidersConcept() {
        // Simulate parent and child registries
        val parentRegistry = ComponentRegistry().apply {
            register("Button") { _, _, _, _, _ -> }
        }
        val childRegistry = ComponentRegistry().apply {
            register("Card") { _, _, _, _, _ -> }
        }

        // Parent has Button but not Card
        assertTrue(parentRegistry.has("Button"))
        assertFalse(parentRegistry.has("Card"))

        // Child has Card but not Button
        assertFalse(childRegistry.has("Button"))
        assertTrue(childRegistry.has("Card"))
    }

    @Test
    fun testConfigWithCustomRegistryAndSurface() {
        val registry = ComponentRegistry().apply {
            register("Column") { _, _, _, _, _ -> }
            register("Text") { _, _, _, _, _ -> }
            register("Button") { _, _, _, _, _ -> }
        }

        val surface = A2UISurface(
            root = "root",
            components = mapOf(
                "root" to A2UIComponent(id = "root", component = "Column"),
                "title" to A2UIComponent(id = "title", component = "Text"),
                "action" to A2UIComponent(id = "action", component = "Button")
            )
        )

        // Verify all component types in the surface have registered renderers
        for ((_, component) in surface.components) {
            assertTrue(
                registry.has(component.component),
                "Registry should have renderer for '${component.component}'"
            )
        }
    }
}
